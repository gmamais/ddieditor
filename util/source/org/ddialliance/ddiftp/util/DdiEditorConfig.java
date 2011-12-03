package org.ddialliance.ddiftp.util;

import org.ddialliance.ddiftp.util.log.LogFactory;
import org.ddialliance.ddiftp.util.log.LogType;

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

public class DdiEditorConfig extends Config {
	// dbxml
	public static final String DBXML_ENVIROMENT_HOME = "dbxml.enviroment.home";
	public static final String DBXML_IMPORT_VALIDATE = "dbxml.import.validate";
	public static final String DBXML_DDI_INDEX = "dbxml.index.ddi";
	public static final String DDI_ELEMENTS_RELATIONSHIP_LIST = "ddi.elements.relationshiplist";
	public static final String CHARSET_ISO = "charset.iso";
	public static final String CHARSET_OEM = "charset.oem";
	public static final String CHARSET_UNICODE = "charset.unicode";

	public static void init() {
		LogFactory.getLog(LogType.SYSTEM, DdiEditorConfig.class).info(
				"DDI editor init config done ;- )");
	}
}
