package org.ddialliance.ddiftp.util.log;

import java.io.IOException;

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
 * An OutputStream that flushes out to a log4j category.
 * Initial developer: Jim Moore
 */
public class Log4jLogOutputStream extends LogOutputStream {
	protected static final String LINE_SEPERATOR = System
			.getProperty("line.separator");

	protected boolean hasBeenClosed = false;

	/**
	 * The internal buffer where data is stored.
	 */
	protected byte[] buf;

	/**
	 * The number of valid bytes in the buffer. This value is always in the
	 * range <tt>0</tt> through <tt>buf.length</tt>; elements
	 * <tt>buf[0]</tt> through <tt>buf[count-1]</tt> contain valid byte
	 * data.
	 */
	protected int count;

	/**
	 * Remembers the size of the buffer for speed.
	 */
	private int bufLength;

	/**
	 * The default number of bytes in the buffer. =2048
	 */
	public static final int DEFAULT_BUFFER_LENGTH = 2048;

	/**
	 * The category to write to.
	 */
	protected Log log;
	
	protected LogLevel logLevel;

	/**
	 * Creates the Log4jOutputStream to flush to the given Category.
	 * 
	 * @param log
	 *            the Logger to write to
	 * @param level
	 *            the Level to use when writing to the Logger
	 * @throws IllegalArgumentException
	 *             if cat == null or priority == null
	 */
	public Log4jLogOutputStream(Log log, LogLevel logLevel) throws IllegalArgumentException {
		super(log, logLevel);
		if (log == null) {
			throw new IllegalArgumentException("cat == null");
		}

		this.log = log;
		this.logLevel = logLevel;
		bufLength = DEFAULT_BUFFER_LENGTH;
		buf = new byte[DEFAULT_BUFFER_LENGTH];
		count = 0;
	}

	/* (non-Javadoc)
	 * @see org.ddialliance.ddiftp.util.log.LogOutputStream#close()
	 */
	public void close() {
		flush();
		hasBeenClosed = true;
	}

	/* (non-Javadoc)
	 * @see org.ddialliance.ddiftp.util.log.LogOutputStream#write(int)
	 */
	public void write(final int b) throws IOException {
		if (hasBeenClosed) {
			throw new IOException("The stream has been closed.");
		}

		// would this be writing past the buffer?

		if (count == bufLength) {
			// grow the buffer
			final int newBufLength = bufLength + DEFAULT_BUFFER_LENGTH;
			final byte[] newBuf = new byte[newBufLength];

			System.arraycopy(buf, 0, newBuf, 0, bufLength);
			buf = newBuf;

			bufLength = newBufLength;
		}

		buf[count] = (byte) b;

		count++;
	}

	/* (non-Javadoc)
	 * @see org.ddialliance.ddiftp.util.log.LogOutputStream#flush()
	 */
	public void flush() {

		if (count == 0) {

			return;
		}

		// don't print out blank lines; flushing from PrintStream puts out these
		if (count == LINE_SEPERATOR.length()) {
			if (((char) buf[0]) == LINE_SEPERATOR.charAt(0)
					&& ((count == 1) || ((count == 2) && ((char) buf[1]) == LINE_SEPERATOR
							.charAt(1)))) {
				reset();
				return;
			}
		}

		final byte[] theBytes = new byte[count];
		System.arraycopy(buf, 0, theBytes, 0, count);
		log.log(logLevel, new String(theBytes));
		reset();
	}

	private void reset() {
		// not resetting the buffer -- assuming that if it grew then it
		// will likely grow similarly again
		count = 0;
	}
}
