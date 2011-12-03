package org.ddialliance.ddiftp.util.xml;

import org.ddialliance.ddiftp.util.Translator;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX DDI3 document handler. Used for access of first element type.
 */
public class DocHandler extends DefaultHandler {
	// change this magic version
	// number to config loaded
	private static String schemaVersion = "3_1";

	public void startElement(String namespaceURI, String sName, String qName,
			Attributes attrs) throws SAXException {
		// element name
		String eName = sName;
		if ("".equals(eName))
			eName = qName;

		// weed out namespace acronym
		String element = null;
		String nameSpaceAcronym = null;
		int index = eName.indexOf(":");
		if (index > -1) {
			element = eName.substring(1 + index);
			nameSpaceAcronym = eName.substring(0, index);
		} else {
			element = eName;
		}

		// retrieve real namespace
		String module = null;
		if (attrs != null) {
			for (int i = 0; i < attrs.getLength(); i++) {
				String aName = attrs.getLocalName(i);
				if ("".equals(aName)) {
					aName = attrs.getQName(i);
				}
				try {
					if (nameSpaceAcronym != null) {
						if (aName.equals("xmlns:" + nameSpaceAcronym)) {
							module = retrieveModulenName(index, attrs, i);
						}
					} else if (aName.equals("xmlns")) {
						module = retrieveModulenName(index, attrs, i);
					}
				} catch (Exception e) {
					throw new SAXException(e.getMessage());
				}
			}
		}
		throw new SAXException(module + "." + element);
	}

	private String retrieveModulenName(int index, Attributes attrs, int i)
			throws Exception {
		String module = null;
		index = attrs.getValue(i).indexOf("ddi:");
		if (index > -1) {
			// check supported ddi version
			int ddiCheck = attrs.getValue(i).lastIndexOf(":");
			if (ddiCheck > -1) {
				String ddiVersion = attrs.getValue(i).substring(ddiCheck + 1,
						attrs.getValue(i).length());
				if (!ddiVersion.equals(schemaVersion)) {
					throw new Exception("***"+(Translator.trans("ddi.version.notsuported", new Object[] {ddiVersion, schemaVersion})));
				}
			}

			int moduleNameEnd = attrs.getValue(i).indexOf(":", 4);
			if (moduleNameEnd == -1) {
				moduleNameEnd = attrs.getValue(i).indexOf("/", 4);
			}
			module = attrs.getValue(i).substring(4, moduleNameEnd);
		}
		return module;
	}
}
