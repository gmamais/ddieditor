package org.ddialliance.ddiftp.util;

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

public class DDIFtpException extends Exception {
	private static final long serialVersionUID = 1L;
	private Object value;
	private Throwable realThrowable;

	/** Default constructor */
	public DDIFtpException() {
	}

	/** Constructs an exception */
	public DDIFtpException(Exception e) {
		super(e);
		realThrowable = e;
		log();
	}

	/** Constructs an exception with a specified message */
	public DDIFtpException(String msg) {
		super(Translator.trans(msg));
		realThrowable = null;
		log();
	}

	/**
	 * Constructs an exception with a specified message with an arg to the
	 * message string
	 */
	public DDIFtpException(String msg, Object obj) {
		super(Translator.trans(msg, obj));
		realThrowable = null;
		log();
	}

	/**
	 * Constructs an exception with a specified message with several args to the
	 * message string
	 */
	public DDIFtpException(String msg, Object[] objArray) {
		super(Translator.trans(msg, objArray));
		realThrowable = null;
		log();
	}

	/** Constructs an exception with a specified message and a throwable cause */
	public DDIFtpException(String msg, Throwable e) {
		super(Translator.trans(msg));
		realThrowable = e;
		log();
	}

	public DDIFtpException(String msg, Throwable e, boolean noLog) {
		super(Translator.trans(msg));
		realThrowable = e;
	}

	/**
	 * Constructs an exception with a specified message with an arg to the
	 * message string and a throwable cause
	 */
	public DDIFtpException(String msg, Object obj, Throwable e) {
		super(Translator.trans(msg, obj));
		realThrowable = e;
		log();
	}

	/**
	 * Constructs an exception with a specified message with several args to the
	 * message string and a throwable cause
	 */
	public DDIFtpException(String msg, Object[] objArray, Throwable e) {
		super(Translator.trans(msg, objArray));
		realThrowable = e;
		log();
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Throwable getRealThrowable() {
		return realThrowable;
	}

	public void setRealThrowable(Throwable realThrowable) {
		this.realThrowable = realThrowable;
	}

	/** Log the exception */
	private void log() {
		Log log = LogFactory.getLog(LogType.EXCEPTION, this.getClass());
		log.info(this.getClass().getName() + ": " + this.getMessage(),
				realThrowable);
	}
}
