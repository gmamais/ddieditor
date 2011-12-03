package org.ddialliance.ddiftp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Properties;
import java.util.Scanner;

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

public class FileUtil {
	final static Log log = LogFactory.getLog(LogType.SYSTEM,
			FileUtil.class.getName());
	private static final int BSIZE = 1024;

	/**
	 * Copy file
	 * 
	 * @param toCopy
	 *            file to copy
	 * @param destination
	 *            destination of file to copy
	 * @throws DDIFtpException
	 */
	public static void copyFile(File toCopy, File destination)
			throws DDIFtpException {
		FileChannel in = null, out = null;
		try {
			in = new FileInputStream(toCopy).getChannel();
			out = new FileOutputStream(destination).getChannel();
			ByteBuffer buffer = ByteBuffer.allocate(BSIZE);
			while (in.read(buffer) != -1) {
				buffer.flip(); // Prepare for writing
				out.write(buffer);
				buffer.clear(); // Prepare for reading
			}
		} catch (Exception e) {
			throw new DDIFtpException("file.error.copy", new Object[] { toCopy,
					destination }, e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e) {
				// do nothing
			}
			try {
				if (out != null) {
					out.close();
				}
			} catch (Exception e) {
				// do nothing
			}
		}
	}

	/**
	 * Retrieve the raw bytes of a file on the local file system
	 * 
	 * @param fileName
	 *            to read
	 * @return Bytes read
	 * @throws Exception
	 */
	public static byte[] readFile(String fileName) throws Exception {
		RandomAccessFile raf = new RandomAccessFile(fileName, "r");
		FileChannel rafFc = raf.getChannel();

		// read
		ByteBuffer byteBuffer = ByteBuffer.allocate(new Long(raf.length())
				.intValue());
		long read = rafFc.read(byteBuffer);

		if (byteBuffer.hasArray()) {
			return byteBuffer.array();
		} else {
			return new byte[0];
		}
	}

	/**
	 * Retrieve the parent directory of a file
	 * 
	 * @param file
	 *            to retrieve parent directory from
	 * @return parent directory
	 * @throws DDIFtpException
	 */
	public static File getParentDir(File file) throws DDIFtpException {
		File result = null;
		try {
			int index = file.getAbsolutePath().lastIndexOf(File.separator);
			String dirPath = file.getAbsolutePath().substring(0, index);
			result = new File(dirPath);
		} catch (Exception e) {
			throw new DDIFtpException("file.error.parentdir", file, e);
		}
		return result;
	}

	/**
	 * Process a file line by line with a file line scanner
	 * 
	 * @param file
	 *            to be processed
	 * @param fileLineScanner
	 *            to process a line in the file
	 * @throws DDIFtpException
	 */
	public static final void processFileLineByLine(File file,
			LineScanner fileLineScanner) throws DDIFtpException {
		Scanner scanner = null;
		try {
			scanner = new Scanner(file);
		} catch (FileNotFoundException e) {
			throw new DDIFtpException("file.notfound", file.getAbsoluteFile(),
					e);
		}

		try {
			while (scanner.hasNextLine()) {
				fileLineScanner.processLine(scanner.nextLine());
			}
		} catch (Exception e) {
			throw new DDIFtpException("Error processing file: "
					+ file.getAbsolutePath() + " line by line", e);
		} finally {
			scanner.close();
		}
	}

	/**
	 * Persist properties to a file
	 * 
	 * @param path
	 *            to file
	 * @param properties
	 *            to store
	 * @throws DDIFtpException
	 */
	public static void storeProperties(String path, Properties properties)
			throws DDIFtpException {
		storeProperties(new File(path), properties);
	}

	/**
	 * Persist properties to a file
	 * 
	 * @param file
	 *            to store in
	 * @param properties
	 *            to store
	 * @throws DDIFtpException
	 */
	public static void storeProperties(File file, Properties properties)
			throws DDIFtpException {
		if (file.exists()) {
			file.delete();
		}

		try {
			FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
			properties.store(fileWriter,
					"Stored by: " + System.getenv().get("USER"));
			fileWriter.close();
		} catch (IOException e) {
			throw new DDIFtpException("Error storing at: "
					+ file.getAbsoluteFile() + " properties: "
					+ properties.toString(), e);
		}
	}

	/**
	 * Load properties from a file
	 * 
	 * @param file
	 *            to load from
	 * @return loaded properties
	 * @throws DDIFtpException
	 */
	public static Properties loadProperties(File file) throws DDIFtpException {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			throw new DDIFtpException("file.notfound", file.getAbsoluteFile(),
					e);
		} catch (IOException e) {
			throw new DDIFtpException("file.ioerror", file.getAbsoluteFile(), e);
		}
		return properties;
	}
}
