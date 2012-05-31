package org.opendatafoundation.data.spss;

/*
 * Author(s): Pascal Heus (pheus@opendatafoundation.org)
 *  
 * This product has been developed with the financial and 
 * technical support of the UK Data Archive Data Exchange Tools 
 * project (http://www.data-archive.ac.uk/dext/) and the 
 * Open Data Foundation (http://www.opendatafoundation.org) 
 * 
 * Copyright 2007-2008 University of Essex (http://www.esds.ac.uk) 
 * 
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or 
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *  
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library; if not, write to the 
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
 * Boston, MA  02110-1301  USA
 * The full text of the license is also available on the Internet at 
 * http://www.gnu.org/copyleft/lesser.html
 * 
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.ddialliance.ddi3.xml.xmlbeans.logicalproduct.LogicalProductDocument;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.ReferenceType;
import org.ddialliance.ddieditor.logic.identification.IdentificationGenerator;
import org.ddialliance.ddieditor.logic.identification.IdentificationManager;
import org.ddialliance.ddieditor.model.DdiManager;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectListDocument;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectListType;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectType;
import org.ddialliance.ddieditor.ui.model.ElementType;
import org.ddialliance.ddiftp.util.DDIFtpException;
import org.opendatafoundation.data.FileFormatInfo;
import org.opendatafoundation.data.Utils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A class to read SPSS files, produce DDI compatible metadata and export the
 * data to ASCII
 */
public class SPSSFile extends RandomAccessFile {
	// TODO change into properties

	// VERSION
	public static final int VERSION_MAJOR = 2008;
	public static final int VERSION_MINOR = 01;

	// DDI 2
	public static final String DDI2_NAMESPACE = "http://www.icpsr.umich.edu/DDI";

	// DDI 3
	public static final String DDI3_ARCHIVE_NAMESPACE = "ddi:archive:3_1";
	public static final String DDI3_INSTANCE_NAMESPACE = "ddi:instance:3_1";
	public static final String DDI3_LOGICAL_PRODUCT_NAMESPACE = "ddi:logicalproduct:3_1";
	public static final String DDI3_PHYSICAL_PRODUCT_NAMESPACE = "ddi:physicaldataproduct:3_1";
	public static final String DDI3_PHYSICAL_INSTANCE_NAMESPACE = "ddi:physicalinstance:3_1";
	public static final String DDI3_PROPRIETARY_RECORD_NAMESPACE = "ddi:physicaldataproduct_proprietary:3_1";
	public static final String DDI3_REUSABLE_NAMESPACE = "ddi:reusable:3_1";
	public static final String DDI3_STUDY_UNIT_NAMESPACE = "ddi:studyunit:3_1";

	// Log
	Boolean logFlag = true; // < Turn logging on/off
	public File logFile; // < Optional logfile. If null, log messages are sent
							// to the console
	Writer logWriter; // < logfile writer
	private long start; // < Used for timing operations
	private long elapsed; // < Used for timing operations

	// SPSS File
	public File file; // < the SPSS File object
	boolean isBigEndian = false; // < Indicates file "endianness" for number
									// storage. Intel processor produced files
									// are little-endian (default).
	// TDOD change into properties
	byte cacheBuffer[] = new byte[4096];
	long cacheStart = -1;
	long cachePointer = -1;
	long cacheEnd = -1;

	// SPSS Metadata
	SPSSRecordType1 infoRecord; // < the SPSS type 1 record
	Map<Integer, SPSSVariable> variableMap; // < list of variables (wraps a
											// SPSSRecordType2)
	SPSSRecordType6 documentationRecord;
	SPSSRecordType7Subtype3 integerInformationRecord;
	SPSSRecordType7Subtype4 floatInformationRecord;
	SPSSRecordType7Subtype5 variableSetsInformationRecord;
	SPSSRecordType7Subtype11 variableDisplayParamsRecord;
	SPSSRecordType7Subtype13 longVariableNamesRecord;
	SPSSRecordType7Subtype14 longStringRecord;
	Map<String, String> longStringRecordMap = new LinkedHashMap<String, String>();
	Map<String, String> longStringRecordLabelMap = new LinkedHashMap<String, String>();

	public boolean isMetadataLoaded = false;

	// SPSS Data (actual values stored in variables)
	long dataStartPosition = -1;
	public boolean isDataLoaded = false;

	private static IdentificationGenerator idGenerator = IdentificationManager
			.getInstance().getIdentificationGenerator();

	public LightXmlObjectType reuseLogicalProductLightXmlObject = null;

	/**
	 * Constructor
	 * 
	 * @param file
	 * @throws FileNotFoundException
	 */
	public SPSSFile(File file) throws FileNotFoundException {
		super(file, "r");
		this.file = file;
	}

	/**
	 * Constructor
	 * 
	 * @param file
	 * @param mode
	 * @throws FileNotFoundException
	 */
	public SPSSFile(File file, String mode) throws FileNotFoundException {
		super(file, mode);
		this.file = file;
	}

	/**
	 * Constructor
	 * 
	 * @param name
	 * @throws FileNotFoundException
	 */
	public SPSSFile(String name) throws FileNotFoundException {
		super(name, "r");
		this.file = new File(name);
	}

	/**
	 * Constructor
	 * 
	 * @param name
	 * @param mode
	 * @throws FileNotFoundException
	 */
	public SPSSFile(String name, String mode) throws FileNotFoundException {
		super(name, "r");
		this.file = new File(name);
	}

	/**
	 * Dumps the file data to the console (for debugging purposes)
	 * 
	 * @throws IOException
	 * @throws SPSSFileException
	 */
	public void dumpData() throws SPSSFileException, IOException {
		dumpData(0);
	}

	/**
	 * Dumps the file data to the console (for debugging purposes)
	 * 
	 * @param nRecords
	 *            the number of records to dump (0 for all)
	 * @throws IOException
	 * @throws SPSSFileException
	 */
	public void dumpData(int nRecords) throws SPSSFileException, IOException {
		dumpData(nRecords, new FileFormatInfo());
	}

	/**
	 * Dumps the file data (for debugging purposes)
	 * 
	 * @param nRecords
	 *            the number of records to dump (0 for all)
	 * @param dataFormat
	 * @throws IOException
	 * @throws SPSSFileException
	 */
	public void dumpData(int nRecords, FileFormatInfo dataFormat)
			throws SPSSFileException, IOException {
		if (nRecords <= 0 || nRecords > this.getRecordCount())
			nRecords = this.getRecordCount();
		log(this.getRecordFromDisk(dataFormat, true));
		for (int i = 2; i <= nRecords; i++) {
			log(this.getRecordFromDisk(dataFormat, false));
		}
	}

	/**
	 * Dumps the DDI2 metadata (for debugging purposes).
	 * 
	 * @throws SPSSFileException
	 * @throws TransformerException
	 * @throws DDIFtpException
	 * @throws TransformerFactoryConfigurationError
	 * @throws DOMException
	 */
	public void dumpDDI2() throws SPSSFileException, TransformerException,
			DOMException, TransformerFactoryConfigurationError, DDIFtpException {
		log(Utils.DOM2String(getDDI2(new FileFormatInfo())));
	}

	/**
	 * Dumps the DDI3 metadata(for debugging purposes).
	 * 
	 * @throws SPSSFileException
	 * @throws TransformerException
	 * @throws DDIFtpException
	 * @throws TransformerFactoryConfigurationError
	 */
	public void dumpDDI3() throws SPSSFileException, TransformerException,
			TransformerFactoryConfigurationError, DDIFtpException {
		log(Utils.DOM2String(getDDI3LogicalProduct(null, null, null)));
		log(Utils.DOM2String(getDDI3PhysicalDataProduct(new FileFormatInfo())));
		log(Utils
				.DOM2String(getDDI3PhysicalInstance(null, new FileFormatInfo())));
	}

	/**
	 * Dumps the file metadata to the console (for debugging purposes)
	 */
	// TODO: this method is incomplete
	public void dumpMetadata() {
		// HEADER
		log(infoRecord.toString());
		// VARIABLES
		Iterator<Integer> varIterator = variableMap.keySet().iterator();
		while (varIterator.hasNext()) {
			SPSSVariable var = variableMap.get(varIterator.next());
			log(var.variableRecord.toString());
			// log(var.valueLabelSet.toString());
		}
		// INFO
		if (documentationRecord != null)
			log(documentationRecord.toString());
		if (integerInformationRecord != null)
			log(integerInformationRecord.toString());
		if (floatInformationRecord != null)
			log(floatInformationRecord.toString());
		if (variableSetsInformationRecord != null)
			log(variableSetsInformationRecord.toString());
		if (variableDisplayParamsRecord != null)
			log(variableDisplayParamsRecord.toString());
		if (longVariableNamesRecord != null)
			log(longVariableNamesRecord.toString());
	}

	/**
	 * Reads the data from the disk and exports a file based on the specified
	 * format
	 * 
	 * @param file
	 * @param dataFormat
	 * @return The number of milliseconds taken to export the file
	 * @throws SPSSFileException
	 * @throws IOException
	 */
	public long exportData(File file, FileFormatInfo dataFormat)
			throws IOException, SPSSFileException {
		// check arguments
		if (file == null) {
			throw new SPSSFileException("File should not be null.");
		}
		if (file.isDirectory()) {
			throw new SPSSFileException("File should not be a directory: "
					+ file);
		}
		// write file
		log("\nExporting data to " + file.getCanonicalPath());
		start = System.currentTimeMillis();
		FileOutputStream fos = new FileOutputStream(file);
		OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");

		// 20070915-PH: added test for empty files
		if (this.infoRecord.numberOfCases > 0) {
			// write header for delimited/CSV ASCII
			if (dataFormat.format == FileFormatInfo.Format.ASCII
					&& (dataFormat.asciiFormat == FileFormatInfo.ASCIIFormat.DELIMITED || dataFormat.asciiFormat == FileFormatInfo.ASCIIFormat.CSV)
					&& dataFormat.namesOnFirstLine) {
				String recordStr = "";
				Iterator<Integer> varIterator = variableMap.keySet().iterator();
				boolean isLongStringVar = false;
				int n = 1;
				while (varIterator.hasNext()) {
					isLongStringVar = false;
					if (n > 1) {
						SPSSVariable var = variableMap.get(varIterator.next());
						if (var.variableRecord != null
								&& longStringRecordLabelMap
										.get(var.variableRecord.label) != null) {
							// var.variableRecord.label
							String orgarName = longStringRecordLabelMap
									.get(var.variableRecord.label);
							if (!orgarName.equals(var.variableName)) {
								isLongStringVar = true;
							}
						}

						if (!isLongStringVar) {
							if (dataFormat.asciiFormat == FileFormatInfo.ASCIIFormat.CSV)
								recordStr += ",";
							else
								recordStr += dataFormat.asciiDelimiter;
							recordStr += var.getName();
						} else {
							System.out.println(var.variableName);
						}
					}
					n++;
				}
				out.write(recordStr + "\n");
			}

			// write data
			out.write(this.getRecordFromDisk(dataFormat, true) + "\n");
			for (int i = 2; i <= this.getRecordCount(); i++) {
				out.write(this.getRecordFromDisk(dataFormat, false) + "\n");
			}
		} else {
			log("WARNING: files does not contain any data");
		}

		// close file
		out.close();

		elapsed = System.currentTimeMillis() - start;
		log("" + elapsed + " ms, file size  " + file.length() / 1024 + "Kb");
		return (elapsed);
	}

	public String createId(ElementType elementType) throws DDIFtpException {
		return idGenerator.generateId(elementType.getIdPrefix(), "");
	}

	public String createId(ElementType elementType, int localId)
			throws DDIFtpException {
		return idGenerator.generateId(
				elementType.getIdPrefix() + idGenerator.getDelimiter()
						+ localId, "");
	}

	public void setLangAttr(Element element) {
		// TODO
	}

	public String getIdDelimiter() {
		return idGenerator.getDelimiter();
	}

	/**
	 * Determines if the data section of the file is compressed
	 * 
	 * @throws SPSSFileException
	 */
	public boolean isCompressed() throws SPSSFileException {
		if (this.infoRecord != null) {
			if (this.infoRecord.compressionSwitch == 0)
				return (false);
			else
				return (true);
		} else
			throw new SPSSFileException("SPSS file not initialized");

	}

	/**
	 * Creates a DDI 2.0 XML Document based for the SPSS data format
	 * 
	 * @return the generated document
	 * @throws SPSSFileException
	 * @throws DDIFtpException
	 * @throws DOMException
	 */
	public Document getDDI2() throws SPSSFileException, DOMException,
			DDIFtpException {
		return (getDDI2(new FileFormatInfo()));
	}

	/**
	 * Creates a DDI 2.0 XML Document based for the specified data file format
	 * 
	 * @return the generated document
	 * @throws SPSSFileException
	 * @throws DDIFtpException
	 * @throws DOMException
	 */
	public Document getDDI2(FileFormatInfo dataFormat)
			throws SPSSFileException, DOMException, DDIFtpException {
		DocumentBuilderFactory domFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder domBuilder;
		Document doc;
		Element elem;
		try {
			domFactory.setNamespaceAware(true);
			domBuilder = domFactory.newDocumentBuilder();
			doc = domBuilder.newDocument();

			/* codeBook */
			Element codeBook = doc.createElementNS(DDI2_NAMESPACE, "codeBook");
			codeBook.setAttribute("version", "2.0");
			codeBook.setAttribute("ID", idGenerator.generateId(null, null));
			doc.appendChild(codeBook);

			// docDscr */
			Element docDscr = (Element) codeBook.appendChild(doc
					.createElementNS(DDI2_NAMESPACE, "docDscr"));
			Element docCitation = (Element) docDscr.appendChild(doc
					.createElementNS(DDI2_NAMESPACE, "citation"));
			Element docTitlStmt = (Element) docCitation.appendChild(doc
					.createElementNS(DDI2_NAMESPACE, "titlStmt"));
			elem = (Element) docTitlStmt.appendChild(doc.createElementNS(
					DDI2_NAMESPACE, "titl"));
			elem.setTextContent("SPSS File " + file.getName());
			Element docProdStmt = (Element) docCitation.appendChild(doc
					.createElementNS(DDI2_NAMESPACE, "prodStmt"));
			// production date
			elem = (Element) docProdStmt.appendChild(doc.createElementNS(
					DDI2_NAMESPACE, "prodDate"));
			String dateStr = new SimpleDateFormat("yyyy-MM-dd")
					.format(new Date());
			elem.setAttribute("date", dateStr);
			elem.setTextContent(dateStr);
			// production software
			elem = (Element) docProdStmt.appendChild(doc.createElementNS(
					DDI2_NAMESPACE, "software"));
			elem.setAttribute("version", "" + VERSION_MAJOR + "."
					+ VERSION_MINOR);
			elem.setTextContent("UKDA/ODaF DExT Tools");

			/* stdyDscr */
			// document production information
			Element stdyDscr = (Element) codeBook.appendChild(doc
					.createElementNS(DDI2_NAMESPACE, "stdyDscr"));
			Element stdyCitation = (Element) stdyDscr.appendChild(doc
					.createElementNS(DDI2_NAMESPACE, "citation"));
			Element stdyTitlStmt = (Element) stdyCitation.appendChild(doc
					.createElementNS(DDI2_NAMESPACE, "titlStmt"));
			elem = (Element) stdyTitlStmt.appendChild(doc.createElementNS(
					DDI2_NAMESPACE, "titl"));
			elem.setTextContent("SPSS File " + file.getName());
			Element stdyProdStmt = (Element) stdyCitation.appendChild(doc
					.createElementNS(DDI2_NAMESPACE, "prodStmt"));
			// production date
			elem = (Element) stdyProdStmt.appendChild(doc.createElementNS(
					DDI2_NAMESPACE, "prodDate"));
			// production software
			elem = (Element) stdyProdStmt.appendChild(doc.createElementNS(
					DDI2_NAMESPACE, "software"));
			elem.setTextContent(this.infoRecord.productIdentification
					.substring(5));

			/* FileDscr */
			Element fileDscr = (Element) codeBook.appendChild(doc
					.createElementNS(DDI2_NAMESPACE, "fileDscr"));
			Element fileTxt = (Element) fileDscr.appendChild(doc
					.createElementNS(DDI2_NAMESPACE, "fileTxt"));
			elem = (Element) fileTxt.appendChild(doc.createElementNS(
					DDI2_NAMESPACE, "fileName"));
			elem.setTextContent(file.getName());
			// dimensions
			Element fileDimensions = (Element) fileTxt.appendChild(doc
					.createElementNS(DDI2_NAMESPACE, "dimensns"));
			elem = (Element) fileDimensions.appendChild(doc.createElementNS(
					DDI2_NAMESPACE, "caseQnty"));
			elem.setTextContent("" + this.infoRecord.numberOfCases);
			elem = (Element) fileDimensions.appendChild(doc.createElementNS(
					DDI2_NAMESPACE, "varQnty"));
			elem.setTextContent("" + this.variableMap.size());
			// file type
			elem = (Element) fileTxt.appendChild(doc.createElementNS(
					DDI2_NAMESPACE, "fileType"));
			elem.setTextContent(this.infoRecord.productIdentification
					.substring(5));

			/* dataDscr */
			Element dataDscr = (Element) codeBook.appendChild(doc
					.createElementNS(DDI2_NAMESPACE, "dataDscr"));
			Iterator<Integer> varIterator = variableMap.keySet().iterator();
			int offset = 1;
			while (varIterator.hasNext()) {
				SPSSVariable var = variableMap.get(varIterator.next());
				dataDscr.appendChild(var.getDDI2(doc, dataFormat, offset));
				offset += var.getLength(dataFormat);
			}
		} catch (ParserConfigurationException e) {
			throw new SPSSFileException("Error creating DDI Document: "
					+ e.getMessage());
		}
		return (doc);
	}

	private String logicalProductDdi3Id = null;

	/**
	 * Returns a default Physical Data Product identifier based on the file
	 * unique identifier.
	 * 
	 * @return a String containing the r:ID
	 * @throws DDIFtpException
	 */
	public String getDDI3DefaultLogicalProductID() throws DDIFtpException {
		// return (this.getUniqueID() + "_" + this.logicalProductIDSuffix);
		if (getLogicalProductDdi3Id() == null) {
			setLogicalProductDdi3Id(IdentificationManager
					.getInstance()
					.getIdentificationGenerator()
					.generateId(ElementType.LOGICAL_PRODUCT.getIdPrefix(), null));
		}
		return getLogicalProductDdi3Id();

	}

	String physicalDataProductId = null;

	/**
	 * Returns a default Physical Data Product identifier based on the file
	 * unique identifier and the data format.
	 * 
	 * @return a String containing the r:ID
	 * @throws DDIFtpException
	 */
	public String getDDI3DefaultPhysicalDataProductID(FileFormatInfo dataFormat)
			throws DDIFtpException {
		// return (this.getUniqueID() + "_" + this.physicalDataProductIDSuffix
		// + "_" + dataFormat.toString());
		if (physicalDataProductId == null) {
			physicalDataProductId = createId(ElementType.PHYSICAL_DATA_PRODUCT);
		}
		return physicalDataProductId;
	}

	String physicalInstanceDdi3Id = null;

	/**
	 * Returns a default Physical Instance identifier based on the file unique
	 * identifier and the data format.
	 * 
	 * @param dataFormat
	 * @return a String containing the r:ID
	 * @throws DDIFtpException
	 */
	public String getDDI3DefaultPhysicalInstanceID(FileFormatInfo dataFormat)
			throws DDIFtpException {
		if (physicalInstanceDdi3Id == null) {
			physicalInstanceDdi3Id = createId(ElementType.PHYSICAL_INSTANCE);
		}
		// return (this.getUniqueID() + "_" + this.physicalInstanceIDSuffix +
		// "_" + dataFormat
		// .toString());
		return physicalInstanceDdi3Id;
	}

	/**
	 * Returns a default Physical Structure Scheme identifier based on the file
	 * unique identifier and the data format.
	 * 
	 * @return a String containing the r:ID
	 * @throws DDIFtpException
	 */
	public String getDDI3DefaultPhysicalStructureSchemeID(
			FileFormatInfo dataFormat) throws DDIFtpException {
		// return (this.getUniqueID() + "_" + this.physicalStructureSchemeSuffix
		// + "_" + dataFormat.toString());
		return createId(ElementType.PHYSICAL_STRUCTURE_SCHEME);
	}

	String recordLayoutSchemeDdi3Id = null;

	public void setRecordLayoutSchemeDdi3Id(String recordLayoutSchemeDdi3Id) {
		this.recordLayoutSchemeDdi3Id = recordLayoutSchemeDdi3Id;
	}

	/**
	 * Returns a default Record Layout Scheme identifier based on the file
	 * unique identifier and the data format.
	 * 
	 * @return a String containing the r:ID
	 * @throws DDIFtpException
	 */
	public String getDDI3DefaultRecordLayoutSchemeID(FileFormatInfo dataFormat)
			throws DDIFtpException {
		// return (this.getUniqueID() + "_" + this.recordLayoutSchemeSuffix +
		// "_" +
		// dataFormat
		// .toString());
		if (recordLayoutSchemeDdi3Id == null) {
			recordLayoutSchemeDdi3Id = createId(ElementType.RECORD_LAYOUT_SCHEME);
		}
		return recordLayoutSchemeDdi3Id;
	}

	String recordLayoutId = null;

	public String getRecordLayoutId() {
		return recordLayoutId;
	}

	public void setRecordLayoutId(String recordLayoutId) {
		this.recordLayoutId = recordLayoutId;
	}

	String variableSchemeId = null;

	/**
	 * Returns a default Variable Scheme identifier based on the file unique
	 * identifier.
	 * 
	 * @return a String containing the r:ID
	 * @throws DDIFtpException
	 */
	public String getDDI3DefaultVariableSchemeID() throws DDIFtpException {
		if (variableSchemeId == null) {
			variableSchemeId = createId(ElementType.VARIABLE_SCHEME);
		}
		return variableSchemeId;
	}

	ReferenceType logicalRecordRef;

	/**
	 * Creates a DDI 3.1 LogiclaProduct XML Document for this file.
	 * 
	 * @param exportOptions
	 *            defines elements to export
	 * @param uniqueID
	 *            a String value for the r:ID element. If null, a unique ID will
	 *            be generated automatically based on the java.util.UUID class
	 * @param identifyingAgency
	 * @return a org.w3c.dom.Document containing the generated Logical Data
	 *         Product
	 * @throws SPSSFileException
	 * @throws DDIFtpException
	 */
	public Document getDDI3LogicalProduct(ExportOptions exportOptions,
			String uniqueID, String identifyingAgency)
			throws SPSSFileException, DDIFtpException {
		DocumentBuilderFactory domFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder domBuilder;
		Document doc;
		Element elem;
		try {
			domFactory.setNamespaceAware(true);
			domBuilder = domFactory.newDocumentBuilder();
			doc = domBuilder.newDocument();

			// Logical Product
			Element logicalProduct = null;
			// lookup
			try {
				LightXmlObjectListType listType = DdiManager.getInstance()
						.getLogicalProductsLight(null, null, null, null)
						.getLightXmlObjectList();
				if (!listType.getLightXmlObjectList().isEmpty()) {
					reuseLogicalProductLightXmlObject = listType
							.getLightXmlObjectList().get(0);
					LogicalProductDocument logicalProductDoc = DdiManager
							.getInstance().getLogicalProduct(
									reuseLogicalProductLightXmlObject.getId(),
									reuseLogicalProductLightXmlObject
											.getVersion(),
									reuseLogicalProductLightXmlObject
											.getParentId(),
									reuseLogicalProductLightXmlObject
											.getParentVersion());
					logicalProduct = (Element) doc.appendChild(doc
							.createElementNS(DDI3_LOGICAL_PRODUCT_NAMESPACE,
									"LogicalProduct"));
					logicalProduct.setNodeValue(logicalProductDoc
							.xmlText(DdiManager.getInstance().getXmlOptions()));

					// attr id-version-agency
					logicalProduct.setAttribute("id", logicalProductDoc
							.getLogicalProduct().getId());
					logicalProduct.setAttribute("version", logicalProductDoc
							.getLogicalProduct().getVersion());
					logicalProduct.setAttribute("agency", logicalProductDoc
							.getLogicalProduct().getAgency());
					// attr version date
					if (logicalProductDoc.getLogicalProduct().getVersionDate() != null) {
						logicalProduct.setAttribute("versionDate",
								logicalProductDoc.getLogicalProduct()
										.getVersionDate().toString());
					}
				}
			} catch (Exception e) {
				throw new DDIFtpException(e);
			}

			// create
			if (logicalProduct == null) {
				logicalProduct = (Element) doc.appendChild(doc.createElementNS(
						DDI3_LOGICAL_PRODUCT_NAMESPACE, "LogicalProduct"));
				Utils.setDDIMaintainableId(logicalProduct,
						getDDI3DefaultLogicalProductID());
			}

			// Create a DataRelationship with a logical record containing all
			// variables
			Element dataRelationship = (Element) logicalProduct.appendChild(doc
					.createElementNS(SPSSFile.DDI3_LOGICAL_PRODUCT_NAMESPACE,
							"DataRelationship"));
			Utils.setDDIIdentifiableId(dataRelationship,
					createId(ElementType.DATA_RELATIONSHIP));

			// Create a logical record containing all variables
			Element logicalRecord = (Element) dataRelationship.appendChild(doc
					.createElementNS(SPSSFile.DDI3_LOGICAL_PRODUCT_NAMESPACE,
							"LogicalRecord"));
			Utils.setDDIIdentifiableId(logicalRecord,
					createId(ElementType.LOGICAL_RECORD));
			logicalRecord.setAttribute("hasLocator", "false");
			logicalRecordRef = Utils.getReference(logicalRecord);

			// Variables in record
			Element varsInRecord = (Element) logicalRecord.appendChild(doc
					.createElementNS(SPSSFile.DDI3_LOGICAL_PRODUCT_NAMESPACE,
							"VariablesInRecord"));
			varsInRecord.setAttribute("allVariablesInLogicalProduct", "true");
			Element variableSchemeReference = (Element) varsInRecord
					.appendChild(doc.createElementNS(
							SPSSFile.DDI3_LOGICAL_PRODUCT_NAMESPACE,
							"VariableSchemeReference"));
			elem = (Element) variableSchemeReference.appendChild(doc
					.createElementNS(SPSSFile.DDI3_REUSABLE_NAMESPACE, "ID"));
			elem.setTextContent(this.getDDI3DefaultVariableSchemeID());
			Utils.addVersionAndAgency(variableSchemeReference, doc);
			//
			// starts var iterator
			//

			// Create category schemes (one per variable with label set)
			Iterator<Integer> varIterator = variableMap.keySet().iterator();
			if (exportOptions.createCategories) {
				while (varIterator.hasNext()) {
					SPSSVariable var = variableMap.get(varIterator.next());
					if (checkForLongString(var)) {
						continue;
					}
					if (var.hasValueLabels()) {
						logicalProduct.appendChild(var
								.createDDI3CategoryScheme(doc));
					}
				}
			}

			// Create code schemes (one per variable with label set)
			// TODO code schemes has to be created for variable without
			// value-labels
			varIterator = variableMap.keySet().iterator();
			while (varIterator.hasNext()) {
				SPSSVariable var = variableMap.get(varIterator.next());
				if (checkForLongString(var)) {
					continue;
				}
				if (var.hasValueLabels()) {
					logicalProduct.appendChild(var.createDDI3CodeScheme(doc,
							exportOptions.createCategories));
				}
			}

			// Create variable scheme (one for all variables)
			Element varScheme = (Element) logicalProduct.appendChild(doc
					.createElementNS(SPSSFile.DDI3_LOGICAL_PRODUCT_NAMESPACE,
							"VariableScheme"));
			Utils.setDDIMaintainableId(varScheme,
					getDDI3DefaultVariableSchemeID());

			// add variables
			varIterator = variableMap.keySet().iterator();
			while (varIterator.hasNext()) {
				SPSSVariable var = variableMap.get(varIterator.next());

				boolean checkForLongString = checkForLongString(var);
				if (checkForLongString) {
					// reset length to max
					NodeList nodeList = varScheme.getChildNodes();
					for (int i = 0; i < nodeList.getLength(); i++) {
						Node node = nodeList.item(i);

						// locate right variable
						String varName = "";
						if (node.getFirstChild().getLocalName()
								.equals("VariableName")) {
							varName = node.getFirstChild().getFirstChild()
									.getNodeValue();
							for (Entry<String, String> entry : longStringRecordMap
									.entrySet()) {
								// System.out.println(varName + " -- "
								// + entry.getKey());

								// Vxx equals Vxx
								if (varName.equals(entry.getKey())) {
									// System.out
									// .println("selected -- " + varName);

									// navigate to representation
									for (Node childNode = node.getFirstChild(); childNode != null;) {
										Node nextChild = childNode
												.getNextSibling();
										if (childNode.getLocalName().equals(
												"Representation")) {
											if (childNode.getFirstChild() != null
													&& childNode
															.getFirstChild()
															.getLocalName()
															.equals("TextRepresentation")) {
												if (childNode
														.getFirstChild()
														.getAttributes()
														.getNamedItem(
																"maxLength") != null) {

													// set max length
													String value = entry
															.getValue();
													childNode
															.getFirstChild()
															.getAttributes()
															.getNamedItem(
																	"maxLength")
															.setNodeValue(value);
												}
											}
										}
										childNode = nextChild;
									}
								}
							}
						}
					}
					continue;
				}

				Element variable = var.createDDI3Variable(doc);
				varScheme.appendChild(variable);

				// System.out.println("name: " + var.getName());
				// System.out.println("var: " + var.getDDI3DataType());
				// System.out.println("Rep.-type: "
				// + var.getDDI3RepresentationType());
			}
		} catch (ParserConfigurationException e) {
			throw new SPSSFileException("Error creating DDI Document: "
					+ e.getMessage());
		}
		return (doc);
	}

	private Element lookupLogicalProduct() throws Exception {
		LightXmlObjectListType listType = DdiManager.getInstance()
				.getLogicalProductsLight(null, null, null, null)
				.getLightXmlObjectList();
		if (!listType.getLightXmlObjectList().isEmpty()) {
			return (Element) (listType.getLightXmlObjectList().get(0)
					.getDomNode());
		}
		return null;
	}

	private boolean checkForLongString(SPSSVariable var) {
		String name = longStringRecordLabelMap.get(var.getLabel());
		if (name != null && !name.equals(var.getName())) {
			return true;
		}
		return false;
	}

	/**
	 * @param logicalProductDdi3Id
	 *            the logicalProductDdi3Id to set
	 */
	public void setLogicalProductDdi3Id(String logicalProductDdi3Id) {
		this.logicalProductDdi3Id = logicalProductDdi3Id;
	}

	/**
	 * @return the logicalProductDdi3Id
	 */
	public String getLogicalProductDdi3Id() {
		return logicalProductDdi3Id;
	}

	/**
	 * Creates a DDI 3.1 PhysicalDataProduct XML Document for this file based on
	 * the specified file format using the default logical product ID.
	 * 
	 * @param dataFormat
	 * @return a org.w3c.dom.Document containing the generated Physical Data
	 *         Product
	 * @throws SPSSFileException
	 * @throws DDIFtpException
	 */
	public Document getDDI3PhysicalDataProduct(FileFormatInfo dataFormat)
			throws SPSSFileException, DDIFtpException {
		return (getDDI3PhysicalDataProduct(dataFormat, null, null, null));
	}

	/**
	 * Creates a DDI 3.1 PhysicalDataProduct XML Document for this file based on
	 * the specified file format.
	 * 
	 * @param dataFormat
	 * @param logicalProductID
	 *            the r:ID of the Logical Product this is refering to
	 * @return a org.w3c.dom.Document containing the generated Physical Data
	 *         Product
	 * @throws SPSSFileException
	 * @throws DDIFtpException
	 */
	public Document getDDI3PhysicalDataProduct(FileFormatInfo dataFormat,
			String logicalProductID) throws SPSSFileException, DDIFtpException {
		return (getDDI3PhysicalDataProduct(dataFormat, logicalProductID, null,
				null));
	}

	/**
	 * Creates a DDI 3.1 PhysicalDataProduct XML Document for this file based on
	 * the specified file format.
	 * 
	 * @param dataFormat
	 * @param logicalProductID
	 *            the r:ID of the Logical Product this is referring to
	 * @param uniqueID
	 *            a String value for the r:ID element. If null, a unique ID will
	 *            be generated automatically based on the java.util.UUID class
	 * @return a org.w3c.dom.Document containing the generated Physical Data
	 *         Product
	 * @throws SPSSFileException
	 * @throws DDIFtpException
	 */
	private Document getDDI3PhysicalDataProduct(FileFormatInfo dataFormat,
			String logicalProductID, String uniqueID) throws SPSSFileException,
			DDIFtpException {
		return (getDDI3PhysicalDataProduct(dataFormat, logicalProductID,
				uniqueID, null));
	}

	/**
	 * Creates a DDI 3.1 PhysicalDataProduct XML Document for this file based on
	 * the specified file format.
	 * 
	 * @param dataFormat
	 * @param logicalProductID
	 *            the r:ID of the Logical Product this is referring to
	 * @param phdpId
	 *            a String value for the r:ID element.
	 * @param identifyingAgency
	 * @return a org.w3c.dom.Document containing the generated Physical Data
	 *         Product
	 * @throws SPSSFileException
	 * @throws DDIFtpException
	 */
	private Document getDDI3PhysicalDataProduct(FileFormatInfo dataFormat,
			String logicalProductID, String phdpId, String identifyingAgency)
			throws SPSSFileException, DDIFtpException {
		DocumentBuilderFactory domFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder domBuilder;
		Document doc;
		Element elem;

		try {
			domFactory.setNamespaceAware(true);
			domBuilder = domFactory.newDocumentBuilder();
			doc = domBuilder.newDocument();

			Element physicalProduct = (Element) doc.appendChild(doc
					.createElementNS(DDI3_PHYSICAL_PRODUCT_NAMESPACE,
							"PhysicalDataProduct"));
			if (phdpId == null)
				phdpId = getDDI3DefaultPhysicalDataProductID(dataFormat);
			Utils.setDDIMaintainableId(physicalProduct, phdpId);

			// Physical structure scheme
			Element physicalStructureScheme = (Element) physicalProduct
					.appendChild(doc.createElementNS(
							DDI3_PHYSICAL_PRODUCT_NAMESPACE,
							"PhysicalStructureScheme"));
			Utils.setDDIMaintainableId(physicalStructureScheme,
					getDDI3DefaultPhysicalStructureSchemeID(dataFormat));

			// Physical Structure
			Element physicalStructure = (Element) physicalStructureScheme
					.appendChild(doc.createElementNS(
							DDI3_PHYSICAL_PRODUCT_NAMESPACE,
							"PhysicalStructure"));
			Utils.setDDIVersionableId(physicalStructure,
					createId(ElementType.PHYSICAL_STRUCTURE));
			// this.physicalStructureID);

			// logical product reference
			Element logicalReference = (Element) physicalStructure
					.appendChild(doc.createElementNS(
							DDI3_PHYSICAL_PRODUCT_NAMESPACE,
							"LogicalProductReference"));
			elem = (Element) logicalReference.appendChild(doc.createElementNS(
					SPSSFile.DDI3_REUSABLE_NAMESPACE, "ID"));
			if (logicalProductID == null)
				logicalProductID = getDDI3DefaultLogicalProductID();
			elem.setTextContent(logicalProductID);
			Utils.addVersionAndAgency(logicalReference, doc);

			// format
			elem = (Element) physicalStructure.appendChild(doc.createElementNS(
					DDI3_PHYSICAL_PRODUCT_NAMESPACE, "Format"));
			elem.setTextContent(dataFormat.toString());

			// decimal separator
			elem = (Element) physicalStructure
					.appendChild(doc.createElementNS(
							DDI3_PHYSICAL_PRODUCT_NAMESPACE,
							"DefaultDecimalSeparator"));
			elem.setTextContent(".");

			// gross record structure
			Element grossRecordStructure = (Element) physicalStructure
					.appendChild(doc.createElementNS(
							DDI3_PHYSICAL_PRODUCT_NAMESPACE,
							"GrossRecordStructure"));
			Utils.setDDIIdentifiableId(
					grossRecordStructure,
					IdentificationManager
							.getInstance()
							.getIdentificationGenerator()
							.generateId(
									ElementType.GROSS_RECORD_STRUCTURE
											.getIdPrefix(), null));
			// this.grossRecordStructureID);
			// 20071005-PH: This element has been removed in RC_001 (is always
			// equal to 1)
			// grossRecordStructure.setAttribute("recordsPerCase","1");
			// TODO: This element is moving to LogicalRecord
			// grossRecordStructure.setAttribute("variableQuantity",""+getVariableCount());
			grossRecordStructure.setAttribute("numberOfPhysicalSegments", "1");

			// LogicalRecordReference refers:
			// LogicalProduct:DataRelationship:LogicalRecord:RecordType:Idenfication.
			Element logicalRecordReference = (Element) grossRecordStructure
					.appendChild(doc.createElementNS(
							DDI3_PHYSICAL_PRODUCT_NAMESPACE,
							"LogicalRecordReference"));
			Utils.setReference(logicalRecordReference, this.logicalRecordRef,
					doc);

			// PhysicalRecordSegment
			Element physicalRecordSegment = (Element) grossRecordStructure
					.appendChild(doc.createElementNS(
							DDI3_PHYSICAL_PRODUCT_NAMESPACE,
							"PhysicalRecordSegment"));
			Utils.setDDIIdentifiableId(
					physicalRecordSegment,
					IdentificationManager
							.getInstance()
							.getIdentificationGenerator()
							.generateId(
									ElementType.PHYSICAL_RECORDSEGMENT
											.getIdPrefix(), null));
			// this.physicalRecordSegmentID);
			physicalRecordSegment.setAttribute("segmentOrder", "1");
			physicalRecordSegment.setAttribute("hasSegmentKey", "false");

			// Record layout scheme
			Element recordLayoutSheme = (Element) physicalProduct
					.appendChild(doc.createElementNS(
							DDI3_PHYSICAL_PRODUCT_NAMESPACE,
							"RecordLayoutScheme"));
			Utils.setDDIMaintainableId(recordLayoutSheme,
					getDDI3DefaultRecordLayoutSchemeID(dataFormat));

			// Record Layout
			Element recordLayout;
			if (dataFormat.format == FileFormatInfo.Format.ASCII) {
				recordLayout = (Element) recordLayoutSheme.appendChild(doc
						.createElementNS(DDI3_PHYSICAL_PRODUCT_NAMESPACE,
								"RecordLayout"));
			} else {
				recordLayout = (Element) recordLayoutSheme.appendChild(doc
						.createElementNS(DDI3_PROPRIETARY_RECORD_NAMESPACE,
								"ProprietaryRecordLayout"));
			}
			recordLayoutId = createId(ElementType.RECORD_LAYOUT);
			Utils.setDDIIdentifiableId(recordLayout, recordLayoutId);

			// References a physical data product
			// and the ID of the physical record segment from that is described
			// by this record layout.
			Element physicalStructureReference = (Element) recordLayout
					.appendChild(doc.createElementNS(
							DDI3_PHYSICAL_PRODUCT_NAMESPACE,
							"PhysicalStructureReference"));
			// id
			elem = (Element) physicalStructureReference.appendChild(doc
					.createElementNS(SPSSFile.DDI3_REUSABLE_NAMESPACE, "ID"));
			elem.setTextContent(phdpId);
			Utils.addVersionAndAgency(physicalStructureReference, doc);

			// physical record segment used
			elem = (Element) physicalStructureReference.appendChild(doc
					.createElementNS(SPSSFile.DDI3_PHYSICAL_PRODUCT_NAMESPACE,
							"PhysicalRecordSegmentUsed"));
			elem.setTextContent(physicalRecordSegment.getAttribute("id"));

			// character set
			String namespace = null;
			if (dataFormat.format.equals(FileFormatInfo.Format.ASCII)) {
				namespace = DDI3_PHYSICAL_PRODUCT_NAMESPACE;
			} else {
				namespace = DDI3_PROPRIETARY_RECORD_NAMESPACE;
			}
			elem = (Element) recordLayout.appendChild(doc.createElementNS(
					namespace, "CharacterSet"));
			elem.setTextContent("ASCII");
			// array base
			elem = (Element) recordLayout.appendChild(doc.createElementNS(
					namespace, "ArrayBase"));
			elem.setTextContent("1");
			// Record Layout (ASCII)
			if (dataFormat.format == FileFormatInfo.Format.ASCII) {
				// Default Variable Scheme
				Element defaultVariableSchemeReference = (Element) recordLayout
						.appendChild(doc.createElementNS(
								DDI3_PHYSICAL_PRODUCT_NAMESPACE,
								"DefaultVariableSchemeReference"));
				elem = (Element) defaultVariableSchemeReference
						.appendChild(doc.createElementNS(
								SPSSFile.DDI3_REUSABLE_NAMESPACE, "ID"));
				elem.setTextContent(getDDI3DefaultVariableSchemeID());
				Utils.addVersionAndAgency(defaultVariableSchemeReference, doc);

				// Data Items
				Iterator<Integer> varIterator = variableMap.keySet().iterator();
				int offset = 1;
				while (varIterator.hasNext()) {
					SPSSVariable var = variableMap.get(varIterator.next());
					recordLayout.appendChild(var.getDDI3DataItem(doc,
							dataFormat, offset));
					if (dataFormat.asciiFormat == FileFormatInfo.ASCIIFormat.FIXED)
						offset += var.getLength(dataFormat);
					else
						offset++;
				}
			} else {
				// SPSS Proprietary Format
				// Software
				Element software = (Element) recordLayout.appendChild(doc
						.createElementNS(DDI3_REUSABLE_NAMESPACE, "Software"));
				Utils.setDDIIdentifiableId(software,
						createId(ElementType.SOFTWARE));

				elem = (Element) software.appendChild(doc.createElementNS(
						SPSSFile.DDI3_REUSABLE_NAMESPACE, "Name"));
				elem.setTextContent(dataFormat.toString());

				// TODO add id attr

				if (integerInformationRecord != null) {
					String version = integerInformationRecord.releaseMajor
							+ "." + integerInformationRecord.releaseMinor;
					if (integerInformationRecord.releaseSpecial > 0)
						version += "."
								+ integerInformationRecord.releaseSpecial;
					elem = (Element) software.appendChild(doc.createElementNS(
							SPSSFile.DDI3_REUSABLE_NAMESPACE, "Version"));
					elem.setTextContent(version);
				}
				elem = (Element) software.appendChild(doc.createElementNS(
						SPSSFile.DDI3_REUSABLE_NAMESPACE, "Description"));
				elem.setTextContent(infoRecord.productIdentification);

				// Default Variable Scheme
				Element defaultVariableSchemeReference = (Element) recordLayout
						.appendChild(doc.createElementNS(
								DDI3_PROPRIETARY_RECORD_NAMESPACE,
								"DefaultVariableSchemeReference"));
				elem = (Element) defaultVariableSchemeReference
						.appendChild(doc.createElementNS(
								SPSSFile.DDI3_REUSABLE_NAMESPACE, "ID"));
				elem.setTextContent(getDDI3DefaultVariableSchemeID());
				Utils.addVersionAndAgency(defaultVariableSchemeReference, doc);

				// ProprietaryInfo
				Element proprietaryInfo = (Element) recordLayout
						.appendChild(doc.createElementNS(
								DDI3_REUSABLE_NAMESPACE, "ProprietaryInfo"));

				elem = (Element) proprietaryInfo.appendChild(doc
						.createElementNS(SPSSFile.DDI3_REUSABLE_NAMESPACE,
								"ProprietaryProperty"));
				elem.setAttribute("name", "Compression");
				elem.setTextContent("" + infoRecord.compressionSwitch);

				elem = (Element) proprietaryInfo.appendChild(doc
						.createElementNS(SPSSFile.DDI3_REUSABLE_NAMESPACE,
								"ProprietaryProperty"));
				elem.setAttribute("name", "CompressionBias");
				elem.setTextContent("" + infoRecord.compressionBias);

				if (integerInformationRecord != null) {
					elem = (Element) proprietaryInfo.appendChild(doc
							.createElementNS(SPSSFile.DDI3_REUSABLE_NAMESPACE,
									"ProprietaryProperty"));
					elem.setAttribute("name", "MachineCode");
					elem.setTextContent(""
							+ integerInformationRecord.machineCode);

					elem = (Element) proprietaryInfo.appendChild(doc
							.createElementNS(SPSSFile.DDI3_REUSABLE_NAMESPACE,
									"ProprietaryProperty"));
					elem.setAttribute("name", "FloatingPointRepresentation");
					elem.setTextContent(""
							+ integerInformationRecord.floatRepresentation
							+ " ["
							+ integerInformationRecord
									.getFloatRepresentationLabel() + "]");

					elem = (Element) proprietaryInfo.appendChild(doc
							.createElementNS(SPSSFile.DDI3_REUSABLE_NAMESPACE,
									"ProprietaryProperty"));
					elem.setAttribute("name", "Endianness");
					elem.setTextContent(""
							+ integerInformationRecord.endianness + " ["
							+ integerInformationRecord.getEndiannessLabel()
							+ "]");

					elem = (Element) proprietaryInfo.appendChild(doc
							.createElementNS(SPSSFile.DDI3_REUSABLE_NAMESPACE,
									"ProprietaryProperty"));
					elem.setAttribute("name", "CharacterSet");
					elem.setTextContent(""
							+ integerInformationRecord.characterRepresentation
							+ " ["
							+ integerInformationRecord
									.getCharacterRepresentationLabel() + "]");
				}
				if (floatInformationRecord != null) {
					elem = (Element) proprietaryInfo.appendChild(doc
							.createElementNS(SPSSFile.DDI3_REUSABLE_NAMESPACE,
									"ProprietaryProperty"));
					elem.setAttribute("name", "Sysmiss");
					elem.setTextContent("" + floatInformationRecord.sysmiss);

					elem = (Element) proprietaryInfo.appendChild(doc
							.createElementNS(SPSSFile.DDI3_REUSABLE_NAMESPACE,
									"ProprietaryProperty"));
					elem.setAttribute("name", "HighestSysmissRecode");
					elem.setTextContent("" + floatInformationRecord.highest);

					elem = (Element) proprietaryInfo.appendChild(doc
							.createElementNS(SPSSFile.DDI3_REUSABLE_NAMESPACE,
									"ProprietaryProperty"));
					elem.setAttribute("name", "LowsetSysmissRecode");
					elem.setTextContent("" + floatInformationRecord.lowest);
				}

				// Data Items
				Iterator<Integer> varIterator = variableMap.keySet().iterator();
				while (varIterator.hasNext()) {
					SPSSVariable var = variableMap.get(varIterator.next());
					recordLayout.appendChild(var
							.getDDI3ProprietaryDataItem(doc));
				}
			}
		} catch (ParserConfigurationException e) {
			throw new SPSSFileException("Error creating DDI Document: "
					+ e.getMessage());
		}
		return (doc);
	}

	/**
	 * Creates a DDI 3.1 PhysicalInstance XML Document for this file based on
	 * the specified file format using default identifiers.
	 * 
	 * @param uri
	 * @param dataFormat
	 * @return a org.w3c.dom.Document containing the generated Physical Data
	 *         Instance
	 * @throws SPSSFileException
	 * @throws DDIFtpException
	 */
	public Document getDDI3PhysicalInstance(URI uri, FileFormatInfo dataFormat)
			throws SPSSFileException, DDIFtpException {
		return (getDDI3PhysicalInstance(uri, dataFormat, null, null, null));
	}

	/**
	 * Creates a DDI 3.1 PhysicalInstance XML Document for this file based on
	 * the specified file format.
	 * 
	 * @param uri
	 * @param dataFormat
	 * @param recordLayoutSchemeID
	 *            the Record Layout Scheme Data Product this instance refers to
	 * @return a org.w3c.dom.Document containing the generated Physical Data
	 *         Instance
	 * @throws SPSSFileException
	 * @throws DDIFtpException
	 */
	private Document getDDI3PhysicalInstance(URI uri,
			FileFormatInfo dataFormat, String recordLayoutSchemeID)
			throws SPSSFileException, DDIFtpException {
		return (getDDI3PhysicalInstance(uri, dataFormat, recordLayoutSchemeID,
				null, null));
	}

	/**
	 * Creates a DDI 3.1 PhysicalInstance XML Document for this file based on
	 * the specified file format.
	 * 
	 * @param uri
	 * @param dataFormat
	 * @param recordLayoutSchemeID
	 *            the Record Layout Scheme Data Product this instance refers to
	 * @param uniqueID
	 *            a String value for the r:ID element. If null, a unique ID will
	 *            be generated automatically based on the java.util.UUID class
	 * @return a org.w3c.dom.Document containing the generated Physical Data
	 *         Instance
	 * @throws SPSSFileException
	 * @throws DDIFtpException
	 */
	private Document getDDI3PhysicalInstance(URI uri,
			FileFormatInfo dataFormat, String recordLayoutSchemeID,
			String uniqueID) throws SPSSFileException, DDIFtpException {
		return (getDDI3PhysicalInstance(uri, dataFormat, recordLayoutSchemeID,
				uniqueID, null));
	}

	/**
	 * Creates a DDI 3.1 PhysicalInstance XML Document for this file based on
	 * the specified file format.
	 * 
	 * @param uri
	 * @param dataFormat
	 * @param recordLayoutSchemeID
	 *            the Record Layout Scheme Data Product this instance refers to
	 * @param uniqueID
	 *            a String value for the r:ID element. If null, a unique ID will
	 *            be generated automatically based on the java.util.UUID class
	 * @param identifyingAgency
	 * @return a org.w3c.dom.Document containing the generated Physical Data
	 *         Instance
	 * @throws SPSSFileException
	 * @throws DDIFtpException
	 */
	private Document getDDI3PhysicalInstance(URI uri,
			FileFormatInfo dataFormat, String recordLayoutSchemeID,
			String uniqueID, String identifyingAgency)
			throws SPSSFileException, DDIFtpException {
		DocumentBuilderFactory domFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder domBuilder;
		Document doc;
		Element elem;

		try {
			domFactory.setNamespaceAware(true);
			domBuilder = domFactory.newDocumentBuilder();
			doc = domBuilder.newDocument();

			Element physicalInstance = (Element) doc.appendChild(doc
					.createElementNS(DDI3_PHYSICAL_INSTANCE_NAMESPACE,
							"PhysicalInstance"));
			if (uniqueID == null)
				uniqueID = getDDI3DefaultPhysicalInstanceID(dataFormat);
			Utils.setDDIMaintainableId(physicalInstance, uniqueID);

			// record layout reference
			Element recordLayoutReference = (Element) physicalInstance
					.appendChild(doc.createElementNS(
							DDI3_PHYSICAL_INSTANCE_NAMESPACE,
							"RecordLayoutReference"));

			// Scheme + ID
			if (recordLayoutSchemeID == null)
				recordLayoutSchemeID = getDDI3DefaultRecordLayoutSchemeID(dataFormat);
			elem = (Element) recordLayoutReference
					.appendChild(doc.createElementNS(
							SPSSFile.DDI3_REUSABLE_NAMESPACE, "Scheme"));
			elem = (Element) elem.appendChild(doc.createElementNS(
					SPSSFile.DDI3_REUSABLE_NAMESPACE, "ID"));
			elem.setTextContent(recordLayoutSchemeID);
			elem = (Element) recordLayoutReference.appendChild(doc
					.createElementNS(SPSSFile.DDI3_REUSABLE_NAMESPACE, "ID"));
			elem.setTextContent(getRecordLayoutId());
			Utils.addVersionAndAgency(recordLayoutReference, doc);

			// data file identification
			Element dataFileIdentification = (Element) physicalInstance
					.appendChild(doc.createElementNS(
							DDI3_PHYSICAL_INSTANCE_NAMESPACE,
							"DataFileIdentification"));
			Utils.setDDIIdentifiableId(dataFileIdentification,
					createId(ElementType.DATA_FILE_IDENTIFICATION));
			// this.physicalInstanceFileID);
			// Master
			if (dataFormat.format == FileFormatInfo.Format.SPSS)
				dataFileIdentification.setAttribute("isMaster", "true");

			if (uri != null) {
				/*
				 * TODO // Name elem = (Element)
				 * dataFileIdentification.appendChild
				 * (doc.createElementNS(SPSSFile
				 * .DDI3_REUSABLE_NAMESPACE,"Name"));
				 * elem.setTextContent(uri.toURL().getFile()); // Path elem =
				 * (Element)
				 * dataFileIdentification.appendChild(doc.createElementNS
				 * (SPSSFile.DDI3_PHYSICAL_INSTANCE_NAMESPACE,"Path"));
				 * elem.setTextContent(uri.getPath());
				 */
				// File URI
				elem = (Element) dataFileIdentification.appendChild(doc
						.createElementNS(
								SPSSFile.DDI3_PHYSICAL_INSTANCE_NAMESPACE,
								"URI"));
				if (uri != null)
					elem.setTextContent(uri.toString());
			}

			// Gross File Structure
			Element grossFile = (Element) physicalInstance.appendChild(doc
					.createElementNS(DDI3_PHYSICAL_INSTANCE_NAMESPACE,
							"GrossFileStructure"));
			Utils.setDDIIdentifiableId(grossFile,
					createId(ElementType.GROSS_FILE_STRUCTURE));
			// this.grossFileID);

			// File statistics
			elem = (Element) grossFile.appendChild(doc.createElementNS(
					SPSSFile.DDI3_PHYSICAL_INSTANCE_NAMESPACE, "CaseQuantity"));
			elem.setTextContent("" + this.getRecordCount());
			elem = (Element) grossFile.appendChild(doc.createElementNS(
					SPSSFile.DDI3_PHYSICAL_INSTANCE_NAMESPACE,
					"OverallRecordCount"));
			elem.setTextContent("" + this.getRecordCount());

		} catch (ParserConfigurationException e) {
			throw new SPSSFileException("Error creating DDI PhysicalInstance: "
					+ e.getMessage());
		} catch (DOMException e) {
			throw new SPSSFileException("Error creating DDI PhysicalInstance: "
					+ e.getMessage());
		}

		return (doc);
	}

	/**
	 * Gets a data record from data in memory based on on the record number and
	 * specified format.
	 * 
	 * @param obsNumber
	 * @param dataFormat
	 * @return A string holding the record values
	 * @throws SPSSFileException
	 */
	public String getRecord(int obsNumber, FileFormatInfo dataFormat)
			throws SPSSFileException {
		if (!isMetadataLoaded)
			throw new SPSSFileException("Metadata has not been loaded");
		if (!isDataLoaded)
			throw new SPSSFileException("Data has not been loaded");
		String recordStr = "";
		if (obsNumber < 1 || obsNumber > this.getRecordCount()) {
			throw new SPSSFileException("Invalid record number [" + obsNumber
					+ ". Range is 1 to " + this.getRecordCount() + "]");
		} else {
			Iterator<Integer> varIterator = variableMap.keySet().iterator();
			int n = 1;
			while (varIterator.hasNext()) {
				// prefix
				if (n > 1) {
					if (dataFormat.asciiFormat == FileFormatInfo.ASCIIFormat.DELIMITED)
						recordStr += dataFormat.asciiDelimiter;
					else if (dataFormat.asciiFormat == FileFormatInfo.ASCIIFormat.CSV)
						recordStr += ",";
				}
				// value
				SPSSVariable var = variableMap.get(varIterator.next());
				recordStr += var.getValueAsString(obsNumber, dataFormat);
				n++;
			}
		}
		return (recordStr);
	}

	/**
	 * Gets a data record in the specified format. If rewind is false, this
	 * assumes the file pointer is the correct location
	 * 
	 * @param dataFormat
	 * @param rewind
	 *            If true start reading from first record otherwise continue
	 *            from current location until end of file is reached
	 * @return A string holding the record values
	 * @throws SPSSFileException
	 * @throws IOException
	 */
	public String getRecordFromDisk(FileFormatInfo dataFormat, boolean rewind)
			throws SPSSFileException, IOException {
		if (!isMetadataLoaded)
			loadMetadata();

		String recordStr = "";

		// read record
		SPSSDataRecord data = new SPSSDataRecord();

		// rewind if necessary
		if (rewind) {
			this.seek(this.dataStartPosition);
			SPSSDataRecord.clusterIndex = 8; // must reset static member of
												// SPSSDataRecord as well
		}

		// Read data
		data.read(this, true);

		// read variables
		Iterator<Integer> varIterator = variableMap.keySet().iterator();
		int n = 1;
		boolean isLongStringVar = false;
		while (varIterator.hasNext()) {
			SPSSVariable var = variableMap.get(varIterator.next());
			isLongStringVar = false;

			// prefix
			if (n > 1) {
				// fix B
				if (var.variableRecord != null
						&& longStringRecordLabelMap
								.get(var.variableRecord.label) != null) {
					String orgarName = longStringRecordLabelMap
							.get(var.variableRecord.label);
					if (!orgarName.equals(var.variableName)) {
						isLongStringVar = true;
					}
				}

				if (!isLongStringVar) {
					if (dataFormat.asciiFormat == FileFormatInfo.ASCIIFormat.DELIMITED)
						recordStr += dataFormat.asciiDelimiter;
					else if (dataFormat.asciiFormat == FileFormatInfo.ASCIIFormat.CSV)
						recordStr += ",";
				}
			}

			// value
			recordStr += var.getValueAsString(0, dataFormat);
			n++;
		}
		return (recordStr);
	}

	/**
	 * Returns the total number of records (cases) in the file.
	 * 
	 * @return the number of records in the file
	 */
	public int getRecordCount() {
		return (this.infoRecord.numberOfCases);
	}

	/**
	 * Gets a SPSSVariable based on its 0-based file index position.
	 * 
	 * @return SPSSVariable
	 * @throws SPSSFileException
	 */
	public SPSSVariable getVariable(int index) {
		if (index >= variableMap.size() || index < 0)
			return (null);
		else
			return ((SPSSVariable) variableMap.values().toArray()[index]);
	}

	/**
	 * Returns the total number of variables in the file.
	 * 
	 * @return the number of records in the file
	 */
	public int getVariableCount() {
		return (this.variableMap.size());
	}

	/**
	 * Load the data section of the file into the variables in memory. This may
	 * be expensive on memory, use with care on large datasets
	 * 
	 * @throws SPSSFileException
	 * @throws IOException
	 */
	public void loadData() throws IOException, SPSSFileException {
		if (this.dataStartPosition < 1) {
			// this has not been initialized, we don't actually know where the
			// data starts
			throw new SPSSFileException(
					"Error: data location pointer not initialized.");

		}
		SPSSDataRecord data = new SPSSDataRecord();
		this.seek(this.dataStartPosition);
		for (int i = 0; i < infoRecord.numberOfCases; i++) {
			// log("\nRECORD "+(i+1)+" offset "+this.getFilePointer());
			data.read(this);
		}
		this.isDataLoaded = true;
	}

	/**
	 * Loads the dictionary and other SPSS metadata from the file
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws SPSSFileException
	 */
	public void loadMetadata() throws FileNotFoundException, IOException,
			SPSSFileException {
		long filePointer;
		int recordType;

		if (this.isMetadataLoaded)
			throw new SPSSFileException("Metadata is already loaded");

		int varIndex = 0;
		this.seek(0);

		// Read the Type 1 record (info)
		this.infoRecord = new SPSSRecordType1();
		this.infoRecord.read(this);
		log(infoRecord.toString());

		// Init Type 2 records map (variables) (need "linked" hash map to retain
		// natural order)
		this.variableMap = new LinkedHashMap<Integer, SPSSVariable>();

		// Read Type 2 records (at least one)
		// This was changed from for(int i=0; i <
		// this.infoRecord.OBSperObservation; i++) {
		// to work the open source SPSSWriter who does not set OBSPerObservation
		int count = 0;
		do {
			// log("reading variableRecord record "+(i+1)+" of " +
			// this.info.OBSperObservation);
			SPSSRecordType2 type2Record = new SPSSRecordType2();
			type2Record.read(this);

			// ignore string continuation records (variableTypeCode = -1)
			// if (type2Record.variableTypeCode == -1) {
			// log(type2Record.toString());
			// System.out.println("break");
			// }
			if (type2Record.variableTypeCode >= 0) {
				log(type2Record.toString());

				// create a new variable
				SPSSVariable var;

				if (type2Record.variableTypeCode == 0) {
					var = new SPSSNumericVariable(this);
				} else {
					var = new SPSSStringVariable(this);
				}

				// saves this record in the variable
				var.variableRecord = type2Record;

				// initialize the variable name
				var.variableShortName = type2Record.name;
				var.variableName = type2Record.name;

				// add variableMap to dictionary
				this.variableMap.put(count, var);
				var.variableNumber = variableMap.size();

				// add missing values as categories
				if (type2Record.missingValueFormatCode > 0) {
					// 1-3 --> discrete missing value codes (up to three)
					for (int j = 0; j < type2Record.missingValueFormatCode; j++) {
						// adding missing values:
						// if the value does not exist as a regular category, we
						// need to create the category
						SPSSVariableCategory cat = var.addCategory(true,
								type2Record.missingValue[j], "");
						cat.isMissing = true;

					}
				} else if (type2Record.missingValueFormatCode <= -2) {
					// -2 --> range of missing value codes
					// Note1: This is only allowed for numeric variable
					// Note2: We assume that the range is made of integral
					// values and increments by 1!!
					int from = (int) SPSSUtils
							.byte8ToDouble(type2Record.missingValue[0]);
					int to = (int) SPSSUtils
							.byte8ToDouble(type2Record.missingValue[1]);
					// TODO code loaded here
					for (int j = from; j <= to; j++) {
						SPSSVariableCategory cat = ((SPSSNumericVariable) var)
								.addCategory(true, (double) j, "");
						cat.isMissing = true;
					}
					if (type2Record.missingValueFormatCode == -3) {
						// -3 --> an extra discrete value is also specified
						SPSSVariableCategory cat = var.addCategory(true,
								type2Record.missingValue[2], "");
						cat.isMissing = true;
					}
				}
			}
			// read next record type
			count++;
			filePointer = this.getFilePointer();
			recordType = this.readSPSSInt();
			this.seek(filePointer);
		} while (recordType == 2);

		if (this.infoRecord.OBSperObservation == -1) {
			// SPSSWriter does not seem to set this value in the
			// Info Record so set it here...
			this.infoRecord.OBSperObservation = count;
		}

		// log
		// log("\n# VARIABLES: " + variableMap.size());
		// Iterator varIterator = variableMap.keySet().iterator();
		// while (varIterator.hasNext()) {
		// SPSSVariable var = variableMap.get(varIterator.next());
		// log(var.getName());
		// }

		// Loop over other records until we find the record type 999
		do {
			// get filePointer and read the record type
			filePointer = this.getFilePointer();
			recordType = this.readSPSSInt();
			switch (recordType) {
			case 3: // Value label sets (and associated variableMap index record
					// type 4)
				// rewind
				this.seek(filePointer);
				// read type 3
				SPSSRecordType3 record3 = new SPSSRecordType3();
				record3.read(this);
				log(record3.toString());

				// read type 4 record (that must follow type 3!)
				SPSSRecordType4 record4 = new SPSSRecordType4();
				record4.read(this);
				log(record4.toString());

				// associate this value label set with variableMap(s) (usually
				// only one variable)
				for (int i = 0; i < record4.numberOfVariables; i++) {
					SPSSVariable var = this.variableMap
							.get(record4.variableIndex[i] - 1); // SPSS
																// variableMap
																// index is
																// 1-based
					var.valueLabelRecord = record3;
					// add each category to the variable list
					Iterator<byte[]> catIterator = record3.valueLabel.keySet()
							.iterator();
					while (catIterator.hasNext()) {
						byte[] key = (byte[]) catIterator.next();
						var.addCategory(false, key, record3.valueLabel.get(key));
					}
				}
				break;
			case 6:
				// rewind
				this.seek(filePointer);
				// read
				SPSSRecordType6 record6 = new SPSSRecordType6();
				record6.read(this);
				log(record6.toString());
				break;
			case 7:
				// read Subtype
				int subrecordType;
				subrecordType = this.readSPSSInt();
				// rewind
				this.seek(filePointer);
				switch (subrecordType) {
				case 3:
					integerInformationRecord = new SPSSRecordType7Subtype3();
					integerInformationRecord.read(this);
					log(integerInformationRecord.toString());
					break;
				case 4:
					floatInformationRecord = new SPSSRecordType7Subtype4();
					floatInformationRecord.read(this);
					log(floatInformationRecord.toString());
					break;
				case 5:
					variableSetsInformationRecord = new SPSSRecordType7Subtype5();
					variableSetsInformationRecord.read(this);
					log(variableSetsInformationRecord.toString());
					break;
				case 11: // Variable display parameters
					variableDisplayParamsRecord = new SPSSRecordType7Subtype11();
					variableDisplayParamsRecord.read(this);
					// log(variableDisplayParamsRecord.toString());
					// update variables
					varIndex = 0;
					for (SPSSRecordType7Subtype11.VariableDisplayParams params : variableDisplayParamsRecord.variableDisplayParams) {
						getVariable(varIndex).measure = params.measure;
						getVariable(varIndex).displayWidth = params.width;
						getVariable(varIndex).alignment = params.alignment;
						varIndex++;
					}
					break;
				case 13: // Long variable names
					longVariableNamesRecord = new SPSSRecordType7Subtype13();
					longVariableNamesRecord.read(this);
					log(longVariableNamesRecord.toString());
					// update variables
					Iterator<Entry<String, String>> it = longVariableNamesRecord.nameMap
							.entrySet().iterator();
					varIndex = 0;
					while (it.hasNext()) {
						Entry<String, String> entry = it.next();
						SPSSVariable var = getVariable(varIndex);
						// make sure the short name matches and that it's not a
						// string continuation
						if (var.variableRecord.variableTypeCode != 0
								&& var.variableShortName.equals(entry.getKey())) {
							var.variableName = (String) entry.getValue();
						} else {
							// log(var.variableShortName+"!="+entry.getKey());
						}
						varIndex++;
					}
					break;
				case 14:
					longStringRecord = new SPSSRecordType7Subtype14();
					longStringRecord.read(this);
					longStringRecordMap.putAll(longStringRecord.nameMap);

					for (String key : longStringRecordMap.keySet()) {
						for (SPSSVariable var : variableMap.values()) {
							if (var.variableName.equals(key)) {
								longStringRecordLabelMap.put(var.getLabel(),
										key);
							}
						}
					}
					break;
				default: // generic type 7
					SPSSRecordType7 record7 = new SPSSRecordType7();
					record7.read(this);
					log(record7.toString());
					break;
				}
				break;
			case 999:
				// end of dictionnary
				// record type 999 contains a single integer equal to 0
				log("\nRECORD TYPE 999 - START OF DATA");
				log("location " + this.getFilePointer());

				if (this.readSPSSInt() != 0)
					throw new SPSSFileException(
							"Error reading record type 999: Non-zero value found.");
				// This location s where the data starts
				this.dataStartPosition = this.getFilePointer();
				break;
			default:
				throw new SPSSFileException("Read error: invalid record type ["
						+ recordType + "]");
			}
		} while (recordType != 999);
		this.isMetadataLoaded = true;
	}

	/**
	 * Logs a message to the console.
	 * 
	 * @param msg
	 * @throws IOException
	 */
	public void log(String msg) {
		if (logFlag) {
			Calendar now = Calendar.getInstance();
			now.setTime(new Date());
			msg = "" + now.get(Calendar.HOUR_OF_DAY) + ":"
					+ now.get(Calendar.MINUTE) + ":" + now.get(Calendar.SECOND)
					+ " " + msg;
			if (this.logFile == null)
				System.out.println(msg);
			else {
				try {
					if (logWriter == null) {
						logWriter = new BufferedWriter(new FileWriter(logFile));
					}
					logWriter.write(msg + "\n");
					logWriter.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Reads a 8-byte IEEE 754 from SPSS file
	 * 
	 * @return the double value read from the file
	 */
	public double readSPSSDouble() throws IOException {
		byte[] buffer = new byte[8];
		if (isBigEndian) {
			// reverse read
			for (int i = 7; i >= 0; i--)
				buffer[i] = this.readByte();
		} else {
			this.read(buffer);
		}
		return (SPSSUtils.byte8ToDouble(buffer));
	}

	/**
	 * Reads a 4-byte integer from the SPSS file
	 * 
	 * @return the integer value read from the file
	 */
	public int readSPSSInt() throws IOException {
		// Ref:
		// http://www.krugle.com/examples/p-y1N2ygsH97tz99a3/EndianConverter.java
		// http://geosoft.no/software/byteswapper/ByteSwapper.java.html
		byte[] buffer = new byte[4];
		if (isBigEndian) {
			// reverse read
			for (int i = 3; i >= 0; i--)
				buffer[i] = this.readByte();
		} else {
			this.read(buffer);
		}
		return (SPSSUtils.byte4ToInt(buffer));
	}

	/**
	 * Reads a string from the SPSS file
	 * 
	 * @param length
	 *            Number of characters to read
	 * @return the String value read from the file
	 */
	public String readSPSSString(int length) throws IOException {
		String s = "";
		byte[] buffer = new byte[length];
		this.read(buffer);
		s = SPSSUtils.byte8ToString(buffer);
		return s;
	}
}
