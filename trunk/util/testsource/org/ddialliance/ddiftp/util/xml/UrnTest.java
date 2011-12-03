package org.ddialliance.ddiftp.util.xml;

import org.junit.Assert;
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

public class UrnTest {

	@Test
	public void nullUrn() throws Exception {
		try {
			Urn urn = new Urn(null, null, null, null, null, null, null, null);
			urn.toUrnString();
			Assert.fail();
		} catch (Exception e) {
			// do nothing
		}
	}

	@Test
	public void maintained() throws Exception {
		Urn urn = new Urn();

		urn.setIdentifingAgency("dda.dk.ddi");
		urn.setMaintainableElement("StudyUnit");
		urn.setMaintainableId("id_1");
		urn.setMaintainableVersion("1.0.0");
		System.out.println(urn.toUrnString());
	}

	@Test
	public void contained() throws Exception {
		Urn urn = new Urn();

		urn.setIdentifingAgency("dda.dk.ddi");
		urn.setMaintainableElement("StudyUnit");
		urn.setMaintainableId("su_1");
		urn.setMaintainableVersion("2.0.0");

		urn.setContainedElement("DataCollection");
		urn.setContainedElementId("dd_1");
		urn.setContainedElementVersion("0.1.1");
		System.out.println(urn.toUrnString());
	}

	@Test
	public void parseUrn() throws Exception {
		String urn1 = "urn:ddi:dda.dk.ddi:StudyUnit.su_1.1.0.1";
		String urn2 = "urn:ddi:dda.dk.ddi:StudyUnit.su_1.1.0.1:QuestionItem.qi_1";
		String urn3 = "urn:ddi:dda.dk.ddi:StudyUnit.su_1.1.0.1:QuestionItem.qi_1.0.1.1";
		String urn4 = "urn:ddi:dda.dk.ddi:StudyUnit.su_1.L.L.L:QuestionItem.qi_1.L.L.L";

		Urn urn = new Urn();
		urn.parseUrn(urn1);
		Assert.assertEquals("Not same!", urn1, urn.toUrnString());

		urn = new Urn();
		urn.parseUrn(urn2);
		Assert.assertEquals("Not same!", urn2, urn.toUrnString());

		urn = new Urn();
		urn.parseUrn(urn3);
		Assert.assertEquals("Not same!", urn3, urn.toUrnString());

		urn = new Urn();
		urn.parseUrn(urn4);
		Assert.assertEquals("Not same!", urn4, urn.toUrnString());
	}
}
