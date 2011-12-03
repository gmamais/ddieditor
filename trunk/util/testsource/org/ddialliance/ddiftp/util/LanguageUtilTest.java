package org.ddialliance.ddiftp.util;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

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

public class LanguageUtilTest {
	@Test
	public void getLanguagesExcludingLanguageSingle() throws Exception {
		String[][] availableLanguages = LanguageUtil.getAvailableLanguages();
		String[][] result = LanguageUtil.getLanguagesExcludingLanguage("da",
				availableLanguages);

		assertEquals(availableLanguages.length - 1, result.length);
	}

	@Test
	public void getLanguagesExcludingLanguageAslist() throws Exception {
		String[][] availableLanguages = LanguageUtil.getAvailableLanguages();
		String[] languagesToRemove = { "da", "en" };

		String[][] result = LanguageUtil.getLanguagesExcludingLanguage(
				Arrays.asList(languagesToRemove), availableLanguages);

		assertEquals(availableLanguages.length - 2, result.length);
	}

	@Test
	public void indexOfLangCode() throws Exception {
		int da = LanguageUtil.indexOfLangCode("da");
		assertEquals(8, da);
	}
}
