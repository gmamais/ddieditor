package org.ddialliance.ddiftp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Properties;

import org.ddialliance.ddiftp.util.log.LogFactory;
import org.ddialliance.ddiftp.util.log.LogType;
import org.ddialliance.ddiftp.util.osgi.Activator;
import org.eclipse.osgi.framework.adaptor.ClassLoaderDelegate;
import org.eclipse.osgi.internal.baseadaptor.DefaultClassLoader;

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

public class Config {
	private static Properties properties = new Properties();
	public static String CONFIG_FILE = "ddiftp-app.properties";
	public static File file = new File("resources" + File.separator
			+ CONFIG_FILE);

	// global
	public static final String ARRAY_DELIMITER = "array.delimiter";

	// xslt
	public static final String XSLT_JAVA_TRANSFORM = "xslt.java.transform";
	public static final String XSLT_IMPLEMENTATION = "xslt.implementation";

	// ddi3
	public static final String DDI3_XML_VERSION = "ddi3_xml_version";
	public static final String DDI3_XMLBEANS_BASEPACKAGE = "ddi3_xmlbeans_basepackage";

	// locale
	public static final String LOCALE_LANGUAGE = "locale.language";
	public static final String LOCALE_COUNTRY = "locale.country";

	// time
	public static final String FORMAT_DATE = "format.date";
	public static final String FORMAT_TIMESTAMP = "format.timestamp";
	public static final String TIME_ZONE = "timezone";
	static {
		init();
	}

	public static void init() {
		// set configs for backwards compatibility
		set(Config.ARRAY_DELIMITER, "__");
		set(Config.DDI3_XML_VERSION, "3.0");
		set(Config.DDI3_XMLBEANS_BASEPACKAGE,
				"org.ddialliance.ddi3.xml.xmlbeans.");
		set(Config.XSLT_JAVA_TRANSFORM,
				"javax.xml.transform.TransformerFactory");
		set(Config.XSLT_IMPLEMENTATION,
				"org.apache.xalan.processor.TransformerFactoryImpl");

		// init sub class by reflection
		String ddiftpConfigClassName = System.getProperty("ddiftp.config");
		if (ddiftpConfigClassName == null) {
			// no sub class to init
			LogFactory
					.getLog(LogType.SYSTEM, Config.class)
					.info("System property: ddiftp.config is empty. No injection class used for configuration");
		} else {
			// class load config
			Class subConfig = null;
			try {
				subConfig = Config.class.getClassLoader().loadClass(
						ddiftpConfigClassName);
			} catch (ClassNotFoundException e) {
				try {
					// osgi config class loading
					ClassLoaderDelegate loader = ((DefaultClassLoader) Activator.class
							.getClassLoader()).getDelegate();
					subConfig = loader.findClass(ddiftpConfigClassName);
				} catch (Exception e2) {
					LogFactory.getLog(LogType.EXCEPTION, Config.class).error(
							"Suggested config: " + ddiftpConfigClassName
									+ " is not found!", e2);
				}
			}

			// invoke init
			if (subConfig != null) {
				try {
					Method m = subConfig.getMethod("init", null);
					m.invoke(null, null);
				} catch (Exception e) {
					e.printStackTrace();
					LogFactory.getLog(LogType.EXCEPTION, Config.class).error(
							e.getMessage(), e);
				}
			} else {
				LogFactory.getLog(LogType.SYSTEM, Config.class).warn(
						"Using default config: " + Config.class.getName());
			}
		}

		try {
			if (new File(CONFIG_FILE).exists()) {
				properties.load(new FileInputStream(new File(CONFIG_FILE)));
			} else if (new File("resources" + File.separator + CONFIG_FILE)
					.exists()) {
				properties.load(new FileInputStream(new File("resources"
						+ File.separator + CONFIG_FILE)));
			}
		} catch (FileNotFoundException e) {
			System.err.println("Couldn't find " + CONFIG_FILE);
		} catch (IOException e) {
			System.err.println("Couldn't load " + CONFIG_FILE);
		}
		LogFactory.getLog(LogType.SYSTEM, Config.class).info(configToString());
	}

	/**
	 * Reinitialize application properties
	 */
	public static void reset() {
		properties.clear();
		init();
	}

	public static void save() throws DDIFtpException {
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			properties.store(new FileOutputStream(file), null);
		} catch (IOException e) {
			throw new DDIFtpException("file.notfound",
					new Object[] { file.getAbsoluteFile() }, e);
		}
	}

	/**
	 * Add configuration values to the property list
	 */
	static void add(Properties p) {
		if (p != null) {
			properties.putAll(p);
		}
	}

	/**
	 * Remove property
	 * 
	 * @param key
	 *            of property to remove
	 */
	public static void remove(String key) {
		if (key != null) {
			properties.remove(key);
		}
	}

	/**
	 * Set configuration value
	 */
	public static void set(String key, String value) {
		properties.setProperty(key, value);
	}

	/**
	 * Get configuration value
	 */
	public static String get(String key) {
		return properties.getProperty(key, "").trim();
	}

	public static int getInt(String key) {
		int intValue;

		try {
			intValue = Integer.parseInt(get(key));
		} catch (NumberFormatException e) {
			intValue = -1;
		}

		return intValue;
	}

	public static long getLong(String key) {
		long longValue;

		try {
			longValue = Long.parseLong(get(key));
		} catch (NumberFormatException e) {
			longValue = -1;
		}

		return longValue;
	}

	public static boolean getBoolean(String key) {
		String value = get(key);

		if (value.equals("")) {
			return false;
		}

		return (value.equals("true") ? true : false);
	}

	public static String[] getArray(String key) {
		String value = get(key);

		String[] result = value.split(Config.get(Config.ARRAY_DELIMITER));
		if (result.length == 0) {
			return new String[] {};
		} else if (result[0].equals("")) {
			return new String[] {};
		}
		for (int i = 0; i < result.length; i++) {
			result[i] = result[i].trim();
		}
		return result;
	}

	public static String configToString() {
		StringBuffer result = new StringBuffer();
		Enumeration<Object> enumration = properties.keys();
		String key;

		while (enumration.hasMoreElements()) {
			key = (String) enumration.nextElement();
			result.append(key + " = " + properties.getProperty(key));

			if (enumration.hasMoreElements()) {
				result.append(", ");
			}
		}

		return result.toString();
	}
}
