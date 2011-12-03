package org.ddialliance.ddiftp.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

public class LanguageUtil {
	/**
	 * Retrieve available languages defined by ISO-639
	 * 
	 * @return list of language, ISO-639-two letter code
	 */
	public static String[][] getAvailableLanguages() {
		Locale[] availLocales = Locale.getAvailableLocales();

		// sort
		Map<String, Locale> localMap = new HashMap<String, Locale>();
		for (int i = 0; i < availLocales.length; i++) {
			localMap.put(availLocales[i].getDisplayLanguage(), availLocales[i]);
		}
		ArrayList<String> toSort = new ArrayList<String>(localMap.keySet());
		Collections.sort(toSort);

		// display
		String[][] entryNamesAndValues = new String[toSort.size()][2];
		int index = 0;
		for (String langCode : toSort) {
			if (localMap.get(langCode).getDisplayLanguage() == null) {
				continue;
			}
			entryNamesAndValues[index][0] = localMap.get(langCode)
					.getDisplayLanguage();

			// String[] isoLangCode = localMap.get(langCode).getISOLanguages();
			// if (isoLangCode.length>1) {
			// for (int i = 0; i < isoLangCode.length; i++) {
			// System.out.println(isoLangCode[i]+ ", ");
			// }
			// } else {
			// entryNamesAndValues[index][1] = isoLangCode[0];
			// }
			entryNamesAndValues[index][1] = localMap.get(langCode)
					.getLanguage();
			index++;
		}
		return entryNamesAndValues;
	}

	public static String[] getLanguages(String[][] availableLanguages) {
		String[] result = new String[availableLanguages.length];
		for (int i = 0; i < availableLanguages.length; i++) {
			result[i] = availableLanguages[i][0];
		}
		return result;
	}

	public static String getLanguage(String langCode,
			String[][] availableLanguages) {
		for (int i = 0; i < availableLanguages.length; i++) {
			if (availableLanguages[i][1].equals(langCode)) {
				return availableLanguages[i][0];
			}
		}
		return "";
	}

	public static Integer getLanguageIndex(String langCode,
			String[][] availableLanguages) {
		for (int i = 0; i < availableLanguages.length; i++) {
			if (availableLanguages[i][1].equals(langCode)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Get available ISO 639 languages excluding specified language
	 * 
	 * @param langCode
	 *            language code to exclude
	 * @return list of available languages
	 */
	public static String[][] getLanguagesExcludingLanguage(String langCode) {
		return getLanguagesExcludingLanguage(langCode, getAvailableLanguages());
	}

	/**
	 * Get available ISO 639 languages excluding specified language
	 * 
	 * @param langCode
	 *            language code to exclude
	 * @param availableLanguages
	 *            list of available languages
	 * @return list of available languages
	 */
	public static String[][] getLanguagesExcludingLanguage(String langCode,
			String[][] availableLanguages) {
		// search
		int remove = -1;
		for (int i = 0; i < availableLanguages.length; i++) {
			if (availableLanguages[i][1].equals(langCode)) {
				remove = i;
				break;
			}
		}

		// remove element
		String[][] result = null;
		if (remove > 0) {
			result = new String[availableLanguages.length - 1][2];
			// copy before element
			System.arraycopy(availableLanguages, 0, result, 0, remove);
			// copy after element
			System.arraycopy(availableLanguages, remove + 1, result, remove,
					result.length - remove);
		}
		return result;
	}

	/**
	 * Get the remaining languages codes not defined in param language used list
	 * 
	 * @param languagesUsed
	 *            list of languages codes to be excluded
	 * @return list of languages codes
	 */
	public static String[][] getLanguagesExcludingLanguage(
			List<String> languagesUsed) {
		return getLanguagesExcludingLanguage(languagesUsed,
				getAvailableLanguages());
	}

	/**
	 * Get the remaining languages codes not defined in param language used list
	 * 
	 * @param languagesUsed
	 *            list of languages codes to be excluded
	 * @param availableLanguages
	 *            list of available languages
	 * @return list of languages codes
	 */
	public static String[][] getLanguagesExcludingLanguage(
			List<String> languagesUsed, String[][] availableLanguages) {
		for (String langCode : languagesUsed) {
			availableLanguages = getLanguagesExcludingLanguage(langCode,
					availableLanguages);
		}
		return availableLanguages;
	}

	public static int indexOfLangCode(String langCode) {
		return indexOfLangCode(langCode, getAvailableLanguages());
	}

	public static int indexOfLangCode(String langCode,
			String[][] availableLanguages) {
		// search
		int found = -1;
		for (int i = 0; i < availableLanguages.length; i++) {
			if (availableLanguages[i][1].equals(langCode)) {
				found = i;
				break;
			}
		}
		return found;
	}
}
