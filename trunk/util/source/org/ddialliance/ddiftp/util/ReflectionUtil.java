package org.ddialliance.ddiftp.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.ddialliance.ddiftp.util.log.Log;
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

/**
 * Reflection utility
 */
public class ReflectionUtil {
	static final Log log = LogFactory.getLog(LogType.SYSTEM,
			ReflectionUtil.class);

	/**
	 * Invoke a static method on a class
	 * 
	 * @param className
	 *            name of class
	 * @param methodName
	 *            name of method
	 * @param args
	 *            arguments to method
	 * @return method return
	 * @throws Exception
	 */
	public static Object invokeStaticMethod(String className,
			String methodName, Object... args) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Invoke method: " + methodName + " on object type: "
					+ className);
		}

		Class[] classArgs = null;
		if (args != null) {
			classArgs = new Class[args.length];
			for (int i = 0; i < args.length; i++) {
				classArgs[i] = args[i].getClass();
			}
		}

		Class c = Class.forName(className);
		Method m = c.getDeclaredMethod(methodName, classArgs);
		Object o = null;
		try {
			o = m.invoke(null, args);
		} catch (InvocationTargetException e) {
			throw e;
		}
		return o;
	}

	/**
	 * Invoke a method on an object
	 * 
	 * @param obj
	 *            to invoke on
	 * @param methodName
	 *            name of method
	 * @param useInterfaceParams
	 *            use interface names arguments
	 * @param args
	 *            arguments of type varargs
	 * @return method return
	 * @throws Exception
	 */
	public static Object invokeMethod(Object obj, String methodName,
			boolean useInterfaceParams, Object... args) throws Exception {
		try {
			Object returnObj = null;

			// sort out params
			Class[] params = extractParams(args, useInterfaceParams);

			// retrieve method
			Method m = obj.getClass().getMethod(methodName, params);

			// execute method
			if (!m.isAccessible()) {
				m.setAccessible(true);
				returnObj = m.invoke(obj, args);
				m.setAccessible(false);
			} else {
				returnObj = m.invoke(obj, args);
			}

			if (log.isDebugEnabled()) {
				log.debug("Invoke method: " + methodName + " on object type: "
						+ obj.getClass().getName() + " result: " + returnObj);
			}
			return returnObj;
		} catch (NoSuchMethodException e) {
			throw e;
		} catch (IllegalAccessException e) {
			throw e;
		} catch (InvocationTargetException e) {
			throw e;
		}
	}

	/**
	 * Extract class names of arguments
	 * 
	 * @param args
	 *            arguments of type varargs
	 * 
	 * @param useInterfaceParams
	 *            use interface names arguments
	 * 
	 * @return array of params
	 */
	public static Class[] extractParams(Object[] args,
			boolean useInterfaceParams) {
		Class[] params = null;
		if (args != null) {
			params = new Class[args.length];
			for (int i = 0; i < args.length; i++) {
				if (useInterfaceParams) {
					// hack to use interface names with params
					for (int j = 0; j < args[i].getClass().getInterfaces().length; j++) {
						params[i] = args[i].getClass().getInterfaces()[i];
					}
				} else {
					if (args[i] == null) {
						params[i] = null;
					} else {
						if (args[i] instanceof Integer)
							params[i] = Integer.TYPE;
						else if (args[i] instanceof Byte)
							params[i] = Byte.TYPE;
						else if (args[i] instanceof Short)
							params[i] = Short.TYPE;
						else if (args[i] instanceof Character)
							params[i] = Character.TYPE;
						else if (args[i] instanceof Long)
							params[i] = Long.TYPE;
						else if (args[i] instanceof Float)
							params[i] = Float.TYPE;
						else if (args[i] instanceof Double)
							params[i] = Double.TYPE;
						else if (args[i] instanceof Boolean)
							params[i] = Boolean.TYPE;
						else
							params[i] = args[i].getClass();
					}
				}
			}
		}
		return params;
	}
}
