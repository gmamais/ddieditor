package org.ddialliance.ddiftp.util.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.ddialliance.ddiftp.util.Config;
import org.ddialliance.ddiftp.util.DDIFtpException;

public class XsltUtil {
	/**
	 * Execute XSLT transformation
	 * 
	 * @param xslFile
	 *            XSLT stylesheet
	 * @param sourceFile
	 *            file to transform/ input
	 * @param outFile
	 *            transformed file/ output
	 * @throws DDIFtpException
	 */
	public static void transform(File xslFile, File sourceFile, File outFile)
			throws DDIFtpException {
		// try { Thread currentThread = Thread.currentThread();
		// currentThread.setContextClassLoader(this.getClass().getClassLoader());
		// }
		// catch (SecurityException e) {}

		// System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
		// "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
		// System.setProperty("javax.xml.parsers.SAXParserFactory",
		// "org.apache.xerces.jaxp.SAXParserFactoryImpl");

		if (System.getProperty(Config.get(Config.XSLT_JAVA_TRANSFORM), "")
				.equals("")) {
			System.setProperty(Config.get(Config.XSLT_JAVA_TRANSFORM), Config
					.get(Config.XSLT_IMPLEMENTATION));
		}

		try {
			Transformer transformer = TransformerFactory.newInstance()
					.newTransformer(
							new StreamSource("resources" + File.separator
									+ "validation.xsl"));
			transformer.transform(
					new StreamSource(sourceFile.getAbsolutePath()),
					new StreamResult(new FileOutputStream(outFile
							.getAbsolutePath())));
		} catch (TransformerConfigurationException e) {
			throw new DDIFtpException("xslt.error.configuration", xslFile
					.getAbsolutePath(), e);
		} catch (TransformerException e) {
			throw new DDIFtpException("validationreport.error.transform",
					new Object[] { sourceFile.getAbsolutePath(),
							xslFile.getAbsolutePath() }, e);
		} catch (FileNotFoundException e) {
			throw new DDIFtpException("file.notfound", "na", e);
		}
	}
}
