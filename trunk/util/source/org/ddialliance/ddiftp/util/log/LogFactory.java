package org.ddialliance.ddiftp.util.log;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;

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

/**
 * Log factory facade to obtain loggers and cache produced loggers.
 * <br>The log factory is independent of backing logging framework used
 * <br>Log factory  currently implements log4j.  
 */
public class LogFactory {
	private static Map<String, Log> instances = new HashMap<String, Log>();
	private static boolean initialized = false;

	private static void initialize() {
		initialized = true;
		PropertyConfigurator.configureAndWatch("resources/log.properties");
	}

	private LogFactory() {
	}

	public static Log getLog(Class logName) {
		if (!initialized)
			initialize();

		Log instance = (Log) instances.get(logName.getName());

		if (instance == null) {
			instance =  new Log4jLog(LogManager.getLogger(logName));
			instances.put(logName.getName(), instance);
		}
		return instance;
	}

	public static Log getLog(LogType logType, Class className) {
		if (!initialized)
			initialize();

		String logName = logType.getLogName() + "." + className.getName();
		Log instance = (Log)instances.get(logName);

		if (instance == null) {
			instance =  new Log4jLog(LogManager.getLogger(logName));
			instances.put(logName, instance);
		}
		return instance;
	}

	public static Log getLog(LogType logType, String subType) {
		if (!initialized)
			initialize();

		String logName = logType.getLogName() + "." + subType;
		Log instance = (Log) instances.get(logName);

		if (instance == null) {
			instance =  new Log4jLog(LogManager.getLogger(logName));
			instances.put(logName, instance);
		}

		return instance;
	}
}
