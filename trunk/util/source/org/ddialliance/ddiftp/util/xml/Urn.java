package org.ddialliance.ddiftp.util.xml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ddialliance.ddiftp.util.DDIFtpException;
import org.ddialliance.ddiftp.util.Translator;
import org.ddialliance.ddiftp.util.log.Log;
import org.ddialliance.ddiftp.util.log.LogFactory;
import org.ddialliance.ddiftp.util.log.LogType;

/**
 * DDI URN: urn: ddi:<br>
 * the maintenance agency:, the maintainable object class, id, and version
 * number, <br>
 * followed by the specific object class, id, and version number (if the object
 * is not maintainable)
 */
public class Urn {
	private static Log log = LogFactory.getLog(LogType.SYSTEM, Urn.class
			.getName());
	private String identifingAgency;

	private String maintainableElement = null;
	private String maintainableId;
	private String maintainableVersion;

	private String containedElement;
	private String containedElementId;
	private String containedElementVersion;

	public Urn() {
	}

	/**
	 * Construct an URN using all arguments
	 * 
	 * @param schemaVersion
	 * @param identifingAgency
	 * @param maintainableElement
	 * @param maintainableId
	 * @param maintainableVersion
	 * @param containedElement
	 * @param containedElementId
	 * @param containedElementVersion
	 */
	public Urn(String schemaVersion, String identifingAgency,
			String maintainableElement, String maintainableId,
			String maintainableVersion, String containedElement,
			String containedElementId, String containedElementVersion) {
		this.identifingAgency = identifingAgency;
		this.maintainableElement = maintainableElement;
		this.maintainableId = maintainableId;
		this.maintainableVersion = maintainableVersion;
		this.containedElement = containedElement;
		this.containedElementId = containedElementId;
		this.containedElementVersion = containedElementVersion;
	}

	/**
	 * Construct an URN based on an URN string
	 * 
	 * @param urnString
	 *            to parse
	 * @throws DDIFtpException
	 */
	public Urn(String urnString) throws DDIFtpException {
		parseUrn(urnString);
	}

	public String getIdentifingAgency() {
		return identifingAgency;
	}

	public void setIdentifingAgency(String identifingAgency) {
		this.identifingAgency = identifingAgency;
	}

	public String getMaintainableElement() {
		return maintainableElement;
	}

	public void setMaintainableElement(String maintainableElement) {
		this.maintainableElement = maintainableElement;
	}

	public String getMaintainableId() {
		return maintainableId;
	}

	public void setMaintainableId(String maintainableId) {
		this.maintainableId = maintainableId;
	}

	public String getMaintainableVersion() {
		return maintainableVersion;
	}

	public void setMaintainableVersion(String maintainableVersion) {
		this.maintainableVersion = maintainableVersion;
	}

	public String getContainedElement() {
		return containedElement;
	}

	public void setContainedElement(String containedElement) {
		this.containedElement = containedElement;
	}

	public String getContainedElementId() {
		return containedElementId;
	}

	public void setContainedElementId(String containedElementId) {
		this.containedElementId = containedElementId;
	}

	public String getContainedElementVersion() {
		return containedElementVersion;
	}

	public void setContainedElementVersion(String containedElementVersion) {
		this.containedElementVersion = containedElementVersion;
	}

	public static boolean validateLocalNameString(String localName) {
		if (localName == null || localName.equals("")) {
			return false;
		}
		Pattern idPattern = Pattern.compile("([a-zA-Z]*)");
		Matcher matcher = idPattern.matcher(localName);
		return matcher.matches();
	}

	/*
	 * [Uu][Rr][Nn]: [Dd][Dd][Ii]: agency [A-Za-z]+\.[A-Za-z][A-Za-z0-9\-]*
	 * element type [A-Z|a-z]+ id [A-Z|a-z]+[A-Z|a-z|0-9|_|\-]* version
	 * ([0-9]+\.[0-9]+\.[0-9]+|[0-9]+\.[0-9]+\.L|[0-9]+\.L\.L|L\.L\.L)
	 */
	public static boolean validateAgencyString(String agency) {
		if (agency == null) {
			return false;
		}
		Pattern idPattern = Pattern.compile("((\\w)+((\\.)?(\\w)*(\\.)?)*)");
		Matcher matcher = idPattern.matcher(agency);
		return matcher.matches();
	}

	/**
	 * Version number of the referenced object, expressed as a three-part
	 * numeric string composed of three positive integers separated by a period.
	 * The first number indicates a major version, the second a minor one the
	 * third integer indicate the minor minor version: 1.0.2.
	 * 
	 * @param version
	 * @return
	 */
	public static boolean validateVersionString(String version) {
		if (version == null) {
			return false;
		}
		// 
		Pattern idPattern = Pattern
				.compile("([0-9]+\\.[0-9]+\\.[0-9]+|[0-9]+\\.[0-9]+\\.L|[0-9]+\\.L\\.L|L\\.L\\.L)");
		Matcher matcher = idPattern.matcher(version);
		return matcher.matches();
	}

	public static boolean validateIdString(String id) {
		if (id == null) {
			return false;
		}
		Pattern idPattern = Pattern
				.compile("([A-Z]|[a-z]|\\*|@|[0-9]|_|$|\\-)*");
		Matcher matcher = idPattern.matcher(id);
		return matcher.matches();
	}

	/**
	 * Parse an DDI URN 
	 * 
	 * @param urnString
	 *            to parse
	 * @throws DDIFtpException
	 *             if URN string is not well formed
	 */
	public void parseUrn(String urnString) throws DDIFtpException {
		if (log.isDebugEnabled()) {
			log.debug("Urn to parse: " + urnString);
		}
		StringBuilder error = new StringBuilder();

		if (urnString == null) {
			throw new DDIFtpException("exception.null", "URN");
		} else if (urnString.equals("")) {
			throw new DDIFtpException("exception.null", "URN");
		}
		String[] splitColon = urnString.split(":");
		// urn:ddi:dda.dk.ddi:StudyUnit.su_1.3.0.0:QuestionItem.dd_1.0.1.1
		// 0 urn:
		// 1 ddi:
		// 2 us.icpsr:
		// 3 DataCollection.DC_5698.2.4.0:
		// 4 TimeMethod_1.1.0.0

		// agency
		if (!validateAgencyString(splitColon[2])) {
			error.append(Translator.trans("urn.agency.invalid",
					new Object[] { splitColon[2] }));
			error.append(" ");
		}
		identifingAgency = splitColon[2];

		// maintainable split
		String[] maintainableSplit = splitColon[3].split("\\.");

		// maintainable element
		if (!validateLocalNameString(maintainableSplit[0])) {
			error.append(Translator.trans("urn.maintained.invalid",
					new Object[] { maintainableSplit[0] }));
			error.append(" ");
		}
		maintainableElement = maintainableSplit[0];

		// maintainable id
		if (!validateIdString(maintainableSplit[1])) {
			error.append(Translator.trans("urn.id.invalid",
					new Object[] { maintainableSplit[1] }));
			error.append(" ");
		}
		maintainableId = maintainableSplit[1];

		// maintainable version
		StringBuilder versionBuilder = new StringBuilder();
		for (int i = 2; i < maintainableSplit.length; i++) {
			versionBuilder.append(maintainableSplit[i]);
			if (i < 4) {
				versionBuilder.append(".");
			}
		}
		maintainableVersion = versionBuilder.toString();
		if (!validateVersionString(maintainableVersion)) {
			error.append(Translator.trans("urn.version.invalid",
					new Object[] { maintainableVersion }));
			error.append(" ");
		}

		// contained element split
		if (splitColon.length > 4) {
			String[] containedSplit = splitColon[4].split("\\.");

			// contained element
			if (!validateLocalNameString(containedSplit[0])) {
				error.append(Translator.trans("urn.contained.invalid",
						new Object[] { containedSplit[0] }));
				error.append(" ");
			}
			containedElement = containedSplit[0];

			// contained id
			if (!validateIdString(containedSplit[1])) {
				error.append(Translator.trans("urn.id.invalid",
						new Object[] { containedSplit[1] }));
				error.append(" ");
			}
			containedElementId = containedSplit[1];

			// contained version
			if (containedSplit.length > 2) {
				versionBuilder.delete(0, maintainableVersion.length());
				versionBuilder = new StringBuilder();
				for (int i = 2; i < containedSplit.length; i++) {
					versionBuilder.append(containedSplit[i]);
					if (i < 4) {
						versionBuilder.append(".");
					}
				}
				containedElementVersion = versionBuilder.toString();
				if (!validateVersionString(containedElementVersion)) {
					error.append(Translator.trans("urn.version.invalid",
							new Object[] { containedElementVersion }));
					error.append(" ");
				}
			}
		}

		if (log.isDebugEnabled()) {
			log.debug("Parsed urn: " + this);
		}
		// error construct
		if (error.length() > 0) {
			DDIFtpException e = new DDIFtpException(error.toString());
			e.setRealThrowable(new Throwable());
			throw e;
		}
	}

	/**
	 * Build formated URN string
	 * 
	 * @return URN string
	 * @throws DDIFtpException
	 *             if not required are present or in case of agency, id, version
	 *             format violation
	 */
	public String toUrnString() throws DDIFtpException {
		StringBuilder error = new StringBuilder();
		StringBuilder result = new StringBuilder();

		// header
		result.append("urn");
		result.append(":ddi:");

		// agency
		if (!validateAgencyString(identifingAgency)) {
			error.append(Translator.trans("urn.agency.invalid",
					new Object[] { identifingAgency }));
			error.append(" ");
		}
		result.append(identifingAgency);
		result.append(":");

		// maintainable element, local name
		if (!validateLocalNameString(maintainableElement)) {
			error.append(Translator.trans("urn.maintained.invalid",
					new Object[] { maintainableElement }));
			error.append(" ");
		}
		result.append(maintainableElement);

		// maintainable element, id
		if (!validateIdString(maintainableId)) {
			error.append(Translator.trans("urn.id.invalid",
					new Object[] { maintainableId }));
			error.append(" ");
		}
		result.append(".");
		result.append(maintainableId);

		// maintainable element, version
		if (!validateVersionString(maintainableVersion)) {
			error.append(Translator.trans("urn.version.invalid",
					new Object[] { maintainableVersion }));
			error.append(" ");
		}
		result.append(".");
		result.append(maintainableVersion);

		// contained element
		if (containedElement != null && !containedElement.equals("")) {
			if (!validateLocalNameString(containedElement)) {
				error.append(Translator.trans("urn.contained.invalid"));
				error.append(" ");
			}
			result.append(":");
			result.append(containedElement);

			// contained element, id
			if (!validateIdString(containedElementId)) {
				error.append(Translator.trans("urn.id.invalid",
						new Object[] { containedElementId }));
				error.append(" ");
			}
			result.append(".");
			result.append(containedElementId);

			// contained element, version
			if (containedElementVersion != null
					&& !containedElementVersion.equals("")) {

				if (!validateVersionString(containedElementVersion)) {
					error.append(Translator.trans("urn.version.invalid",
							new Object[] { containedElementVersion }));
					error.append(" ");
				}
				result.append(".");
				result.append(containedElementVersion);
			}
		}

		// error construct
		if (error.length() > 0) {
			log.error(result.toString());
			DDIFtpException e = new DDIFtpException(error.toString());
			e.setRealThrowable(new Throwable());
			throw e;
		}
		return result.toString();
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("IdentifingAgency: ");
		result.append(identifingAgency);

		result.append(", maintainableElement: ");
		result.append(maintainableElement);
		result.append(", maintainableId: ");
		result.append(maintainableId);
		result.append(", maintainableVersion: ");
		result.append(maintainableVersion);

		result.append(", containedElement: ");
		result.append(containedElement);
		result.append(", containedElementId: ");
		result.append(containedElementId);
		result.append(", containedElementVersion: ");
		result.append(containedElementVersion);

		return result.toString();
	}
}
