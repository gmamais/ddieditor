package org.ddialliance.ddiftp.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

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
 * ResourceBundleManager manages several resource bundles and provides a uniform
 * access to them
 */
public abstract class ResourceBundleManager extends ResourceBundle {
	public static final String BUNDLE_DIRECTORY = "resources/";
	protected String[] baseName;
	private static ResourceBundleManagerImpl instance;
	private static List<ResourceBundle> bundles = new ArrayList<ResourceBundle>();

	/**
	 * Constructs and sets the resource bundle base names array as empty
	 */
	public ResourceBundleManager() {
		this(new String[0]);
	}

	/**
	 * Constructs and sets the resource bundle base names as an array
	 * 
	 * @param baseName
	 *            array of basenames
	 */
	protected ResourceBundleManager(String[] baseName) {
		this.baseName = baseName;
	}

	/**
	 * Constructs and sets the resource bundle base names as an array from a
	 * string like: test1,test2 etc or test1 test2 etc
	 * 
	 * @param baseName
	 *            string of basenames
	 */
	protected ResourceBundleManager(String baseName) {
		buildBaseName(baseName, " ,");
	}

	/**
	 * Retrieve all the added resource bundles as one resource bundle
	 * 
	 * @return resource bundle
	 */
	public static ResourceBundle getBundle() {
		if (instance == null)
			instance = new ResourceBundleManagerImpl();
		return instance;
	}

	/**
	 * Retrieve base names of loaded resource bundles
	 * 
	 * @return base name array
	 */
	public String[] getBaseName() {
		return baseName;
	}

	/**
	 * Adds a resource bundle to the collection of bundles
	 * 
	 * @param bundle
	 */
	public static void addResourceBundle(ResourceBundle bundle) {
		bundles.add(bundle);
	}

	/**
	 * Removes a resource bundle from the collection of bundles
	 * 
	 * @param bundle
	 */
	public static void removeResourceBundle(ResourceBundle bundle) {
		bundles.remove(bundle);
	}

	/**
	 * Is there added any resource bundles
	 * 
	 * @return status
	 */
	public static boolean isEmptyBundle() {
		return bundles.isEmpty();
	}

	public abstract Enumeration getKeys();

	protected abstract Object handleGetObject(String key);

	/**
	 * Generate resource bundle base names array
	 * 
	 * @param baseName
	 * @param delimiter
	 */
	protected void buildBaseName(String baseName, String delimiter) {
		String s = null;
		try {
			s = System.getProperty(baseName);
			if (s == null)
				return;
			StringTokenizer st = new StringTokenizer(s, delimiter);
			this.baseName = new String[st.countTokens()];
			int i = 0;
			while (st.hasMoreTokens())
				this.baseName[i++] = st.nextToken().trim();
		} catch (Exception e) {
			throw new RuntimeException("Can not resolve base name: " + s);
		}
	}

	public static void addResourceBundleFromProperties(String baseName,
			Locale locale) {
		String[] test = {
				baseName + "_" + locale.getLanguage() + "_"
						+ locale.getCountry() + "_" + locale.getVariant()
						+ ".properties",
				baseName + "_" + locale.getLanguage() + "_"
						+ locale.getCountry() + ".properties",
				baseName + "_" + locale.getLanguage() + ".properties",
				baseName + ".properties" };

		File file;
		for (int i = 0; i < test.length; i++) {
			file = new File(test[i]);
			if (file.exists()) {
				try {
					addResourceBundle(new PropertyResourceBundle(
							new FileInputStream(file)));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Default implementation of ResourceBundleManager
	 */
	static class ResourceBundleManagerImpl extends ResourceBundleManager {
		public ResourceBundleManagerImpl(String[] baseName) {
			super(baseName);
		}

		public ResourceBundleManagerImpl(String baseName) {
			super(baseName);
		}

		public ResourceBundleManagerImpl() {
			this(new String[0]);
		}

		public Enumeration getKeys() {
			return new Enumeration() {
				Enumeration enumeration = null;
				int i = 0;

				public boolean hasMoreElements() {
					boolean b = false;
					while (enumeration == null
							|| !(b = enumeration.hasMoreElements())) {
						if (i >= bundles.size()) {
							enumeration = null;
							return b;
						}
						enumeration = ((ResourceBundle) bundles.get(i++))
								.getKeys();
					}
					return b;
				}

				public Object nextElement() {
					if (enumeration == null)
						throw new NoSuchElementException();
					return enumeration.nextElement();
				}
			};
		}

		protected Object handleGetObject(String key) {
			ResourceBundle rb = null;
			String val = null;
			for (int i = 0; i < bundles.size(); i++) {
				rb = (ResourceBundle) bundles.get(i);
				try {
					val = rb.getString(key);
				} catch (Exception e) {
				}
				if (val != null)
					break;
			}
			return val;
		}
	}
}
