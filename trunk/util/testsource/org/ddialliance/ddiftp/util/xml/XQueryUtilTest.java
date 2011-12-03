package org.ddialliance.ddiftp.util.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.apache.xmlbeans.XmlCursor;
import org.ddialliance.ddi3.xml.xmlbeans.instance.DDIInstanceDocument;
import org.ddialliance.ddi3.xml.xmlbeans.logicalproduct.BaseLogicalProductType;
import org.junit.Before;
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

public class XQueryUtilTest {
	DDIInstanceDocument doc = null;
	XQueryUtil xqueryUtil = null;

	@Before
	public void test() {
		try {
			doc = DDIInstanceDocument.Factory.parse(new File(
					"resources/testfile_2.xml"));
			xqueryUtil = new XQueryUtil(doc);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void generateXNamespaceDeclerations() throws Exception {
		assertTrue(!xqueryUtil.getNamespaces().isEmpty());
	}

	@Test
	public void getXPath() {
		String xpath = xqueryUtil.getXPath(doc.getDDIInstance().getDomNode());
		assertEquals("/ns1:DDIInstance[1]", xpath);
	}

	@Test
	public void selectXPath() throws Exception {
		String query = "/ns1:DDIInstance[1]/s:StudyUnit[1]/l:LogicalProduct[1]";
		XmlCursor xmlCursor = xqueryUtil.selectXPath(query, doc, true);
		while (xmlCursor.hasNextSelection()) {
			xmlCursor.toNextSelection();
			assertEquals("ID_55499c11-4288-4cb5-89f0-0c57a7eeeb30_LogPrd",
					((BaseLogicalProductType) xmlCursor.getObject()).getId());
			String computeXPath = xqueryUtil.getXPath(xmlCursor.getObject()
					.getDomNode());
			assertEquals(query, computeXPath);
		}
	}
}
