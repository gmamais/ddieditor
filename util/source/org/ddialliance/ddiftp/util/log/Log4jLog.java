package org.ddialliance.ddiftp.util.log;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

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
 * Wrapper logger for log4j logger
 */
public final class Log4jLog implements Log {
	final Logger logger;

	final static String FQCN = Log4jLog.class.getName();

	final boolean traceCapable;

	protected Log4jLog(org.apache.log4j.Logger logger) {
		this.logger = logger;
		traceCapable = isTraceCapable();
	}

	private boolean isTraceCapable() {
		try {
			logger.isTraceEnabled();
			return true;
		} catch (NoSuchMethodError e) {
			return false;
		}
	}

	public Logger getLogger() {
		return logger;
	}
	/**
	 * Get logger name
	 * 
	 * @return name
	 */
	public String getName() {
		return logger.getName();
	}

	/**
	 * Is this logger instance enabled for the TRACE level?
	 * 
	 * @return True if this Logger is enabled for level TRACE, false otherwise.
	 */
	public boolean isTraceEnabled() {
		if (traceCapable) {
			return logger.isTraceEnabled();
		} else {
			return logger.isDebugEnabled();
		}
	}

	public void trace(Object obj) {
		logger.log(FQCN, traceCapable ? Level.TRACE : Level.DEBUG, obj, null);
	}

	public void trace(Object obj, Throwable t) {
		logger.log(FQCN, traceCapable ? Level.TRACE : Level.DEBUG, obj, t);
	}

	/**
	 * Is this logger instance enabled for the DEBUG level?
	 * 
	 * @return True if this Logger is enabled for level DEBUG, false otherwise.
	 */
	public boolean isDebugEnabled() {
		return logger.isDebugEnabled();
	}

	public void debug(Object obj) {
		logger.log(FQCN, Level.DEBUG, obj, null);
	}

	public void debug(Object obj, Throwable t) {
		logger.log(FQCN, Level.DEBUG, obj, t);
	}

	/**
	 * Is this logger instance enabled for the INFO level?
	 * 
	 * @return True if this Logger is enabled for the INFO level, false
	 *         otherwise.
	 */
	public boolean isInfoEnabled() {
		return logger.isInfoEnabled();
	}

	public void info(Object obj) {
		logger.log(FQCN, Level.INFO, obj, null);
	}

	public void info(Object obj, Throwable t) {
		logger.log(FQCN, Level.INFO, obj, t);
	}

	/**
	 * Is this logger instance enabled for the WARN level?
	 * 
	 * @return True if this Logger is enabled for the WARN level, false
	 *         otherwise.
	 */
	public boolean isWarnEnabled() {
		return logger.isEnabledFor(Level.WARN);
	}

	public void warn(Object obj) {
		logger.log(FQCN, Level.WARN, obj, null);
	}

	public void warn(Object obj, Throwable t) {
		logger.log(FQCN, Level.WARN, obj, t);
	}

	/**
	 * Is this logger instance enabled for level ERROR?
	 * 
	 * @return True if this Logger is enabled for level ERROR, false otherwise.
	 */
	public boolean isErrorEnabled() {
		return logger.isEnabledFor(Level.ERROR);
	}

	public void error(Object obj) {
		logger.log(FQCN, Level.ERROR, obj, null);
	}

	public void error(Object obj, Throwable t) {
		logger.log(FQCN, Level.ERROR, obj, t);
	}

	/**
	 * Fatal is not part of log4j
	 * 
	 * @return defaults to true
	 */
	public boolean isFatalEnabled() {
		return true;
	}

	public void fatal(Object obj) {
		logger.log(FQCN, Level.ERROR, obj, null);
	}

	public void fatal(Object obj, Throwable t) {
		logger.log(FQCN, Level.ERROR, obj, t);
	}

	public void log(LogLevel logLevel, Object obj) {
		logImpl(logLevel, obj, null);
	}

	public void log(LogLevel logLevel, Object obj, Throwable throwable) {
		logImpl(logLevel, obj, throwable);
	}

	private void logImpl(LogLevel logLevel, Object obj, Throwable throwable) {
		if (logLevel.equals(LogLevel.DEBUG)) {
			debug(obj, throwable);
		} else if (logLevel.equals(LogLevel.ERROR)) {
			error(obj, throwable);
		} else if (logLevel.equals(LogLevel.FATAL)) {
			fatal(obj, throwable);
		} else if (logLevel.equals(LogLevel.INFO)) {
			info(obj, throwable);
		} else if (logLevel.equals(LogLevel.TRACE)) {
			trace(obj, throwable);
		} else if (logLevel.equals(LogLevel.WARN)) {
			warn(obj, throwable);
		}
	}
}
