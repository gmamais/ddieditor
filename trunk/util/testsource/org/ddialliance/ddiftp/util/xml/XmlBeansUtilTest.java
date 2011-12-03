package org.ddialliance.ddiftp.util.xml;

import java.io.File;

import junit.framework.Assert;

import org.apache.xmlbeans.XmlObject;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.DisplayTextDocument;
import org.ddialliance.ddi3.xml.xmlbeans.datacollection.DynamicTextType;
import org.ddialliance.ddi3.xml.xmlbeans.group.AbstractDocument;
import org.ddialliance.ddi3.xml.xmlbeans.instance.DDIInstanceDocument;
import org.ddialliance.ddi3.xml.xmlbeans.instance.DDIInstanceType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.LabelType;
import org.ddialliance.ddi3.xml.xmlbeans.reusable.StructuredStringType;
import org.ddialliance.ddi3.xml.xmlbeans.studyunit.StudyUnitType;
import org.ddialliance.ddieditor.model.lightxmlobject.LightXmlObjectType;
import org.junit.Test;

/*
* Copyright 2011 Danish Data Archive (http://www.dda.dk) 
* 
* This program is free software; you can redistribute it and/or modify it 
* under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation; either Version 3 of the License, or 
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
*/

public class XmlBeansUtilTest {
	@Test
	public void openSaveDDIDoc() throws Exception {
		XmlObject xmlObject = XmlBeansUtil.openDDIDoc(new File(
				"resources/testfile_2.xml"));
		Assert.assertTrue(xmlObject instanceof DDIInstanceDocument);

		File testFile = new File("resources/xmlBeansUtilTest.xml");
		XmlBeansUtil.saveDDIDoc(xmlObject, testFile);
		Assert.assertTrue(testFile.exists());

		XmlObject testXmlObject = XmlBeansUtil.openDDIDoc(testFile);
		testFile.deleteOnExit();
		Assert.assertNotNull(testXmlObject);
		// xml formating issues
		// Assert.assertEquals("Not the same from open - save!",
		// xmlObject.xmlText().trim(), testXmlObject.xmlText().trim());
	}

	@Test
	public void nodeToXmlObject() throws Exception {
		DDIInstanceDocument doc = (DDIInstanceDocument) XmlBeansUtil
				.openDDIDoc(new File("resources/testfile_2.xml"));

		StudyUnitType studyUnitType = XmlBeansUtil.nodeToXmlObject(
				StudyUnitType.class, doc.getDDIInstance().getStudyUnitArray(0)
						.getDomNode());
		Assert.assertNotNull(studyUnitType);
	}

	@Test
	public void setTextOnMixedElement() throws Exception {
		DDIInstanceDocument doc = (DDIInstanceDocument) XmlBeansUtil
				.openDDIDoc(new File("resources/testfile_2.xml"));
		org.w3.x1999.xhtml.PType p = doc.getDDIInstance().getStudyUnitArray(0)
				.getAbstractArray(0).getContent().getPArray(0);
		String test = "Setting text on a mixed content element";
		p = (org.w3.x1999.xhtml.PType) XmlBeansUtil.setTextOnMixedElement(p,
				test);
		Assert.assertEquals(test, XmlBeansUtil.getTextOnMixedElement(p));
	}

	@Test
	public void setTextOnNewMixedElement() throws Exception {
		LabelType label = LabelType.Factory.newInstance();
		label.setLang("da");
		String test = "Setting text on a mixed content element";
		label = (LabelType) XmlBeansUtil.setTextOnMixedElement(label, test);
		Assert.assertEquals(test, XmlBeansUtil.getTextOnMixedElement(label));
	}

	@Test
	public void setTextOnMixedElementWithNamespace() throws Exception {
		String xml = "<Abstract xmlns=\"ddi:group:3_0\" id=\"ABSTRACT\"><Content xmlns=\"ddi:reusable:3_0\">Study description not available.</Content></Abstract>";
		String text = "new text";
		AbstractDocument doc = AbstractDocument.Factory.parse(xml);

		StructuredStringType result = (StructuredStringType) XmlBeansUtil
				.setTextOnMixedElement(doc.getAbstract().getContent(), text);
		Assert.assertEquals(text, XmlBeansUtil.getTextOnMixedElement(result));
	}

	@Test
	public void instanceOfXmlBeanDocument() throws Exception {
		DDIInstanceDocument doc = (DDIInstanceDocument) XmlBeansUtil
				.openDDIDoc(new File("resources/testfile_2.xml"));
		try {
			XmlBeansUtil.instanceOfXmlBeanDocument(doc, new Throwable());
		} catch (Exception e) {
			Assert.fail();
		}
	}

	@Test
	public void getXmlObjectTypeFromXmlDocument() throws Exception {
		DDIInstanceDocument doc = (DDIInstanceDocument) XmlBeansUtil
				.openDDIDoc(new File("resources/testfile_2.xml"));
		XmlObject docType = XmlBeansUtil.getXmlObjectTypeFromXmlDocument(doc,
				new Throwable());
		Assert.assertTrue("Not instanceof", docType instanceof DDIInstanceType);
	}

	@Test
	public void addTranslationAttributes() throws Exception {
		DynamicTextType displayText = DisplayTextDocument.Factory.newInstance()
				.addNewDisplayText();
		displayText = XmlBeansUtil.addTranslationAttributes(displayText, "da",
				true, true);
		System.out.println(displayText);
	}

	@Test
	public void getXmlAttributeValue() throws Exception {
		LightXmlObjectType lightXmlObject = LightXmlObjectType.Factory
				.newInstance();

		// version not set
		String result = XmlBeansUtil.getXmlAttributeValue(
				lightXmlObject.toString(), "version=\"");
		Assert.assertEquals("Version not set!", null, result);

		// version set as null
		lightXmlObject.setVersion(null);
		result = XmlBeansUtil.getXmlAttributeValue(lightXmlObject.toString(),
				"version=\"");
		Assert.assertEquals("Version set as null!", null, result);

		// version set as empty string
		lightXmlObject.setVersion("");
		result = XmlBeansUtil.getXmlAttributeValue(lightXmlObject.toString(),
				"version=\"");
		Assert.assertEquals("Version set as empty string!", null, result);
	}

	@Test
	public void addXsiAttributes() throws Exception {
		DDIInstanceDocument ddiInstanceDoc = DDIInstanceDocument.Factory
				.newInstance();
		ddiInstanceDoc.addNewDDIInstance();
		XmlBeansUtil.addXsiAttributes(ddiInstanceDoc);
		String xsi = XmlBeansUtil.getXmlAttributeValue(
				ddiInstanceDoc.xmlText(), "xmlns:xsi=\"");
		String xsiSchemaLocation = XmlBeansUtil.getXmlAttributeValue(
				ddiInstanceDoc.xmlText(), "xsi:schemaLocation=\"");
		// System.out.println(ddiInstanceDoc.getDDIInstance());
	}
}
