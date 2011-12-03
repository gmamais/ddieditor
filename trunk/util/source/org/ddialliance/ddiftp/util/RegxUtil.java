package org.ddialliance.ddiftp.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public class RegxUtil {
	private static Pattern pattern;
	private static Matcher matcher;

	/**
	 * Constructor
	 * 
	 * @param regex
	 *            to search for
	 * @param text
	 *            to search in
	 */
	public RegxUtil(String regex, String text) {
		setPattern(regex);
		setMatcher(text);
	}

	public Pattern getPattern() {
		return pattern;
	}

	public static void setPattern(String regex) {
		pattern = Pattern.compile(regex);
	}

	public Matcher getMatcher() {
		return matcher;
	}

	public static void setMatcher(String text) {
		matcher = pattern.matcher(text);
	}

	public boolean find() {
		return matcher.find();
	}

	/**
	 * Is the regx in text
	 * 
	 * @param regex
	 *            to search for
	 * @param text
	 *            to search in
	 * @return result
	 */
	public static boolean find(String regex, String text) {
		setPattern(regex);
		setMatcher(text);
		return matcher.find();
	}
}
