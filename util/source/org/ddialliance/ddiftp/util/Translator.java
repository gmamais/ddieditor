package org.ddialliance.ddiftp.util;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ddialliance.ddiftp.util.log.Log;
import org.ddialliance.ddiftp.util.log.LogFactory;
import org.ddialliance.ddiftp.util.log.LogType;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.DurationFieldType;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;

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
 * Handles internationalization, i18n and localization, l10n
 */
public class Translator {

	protected static final String DEFAULT_BUNDLE = null;

	/**
	 * Map of previously created MessageFormat objects.
	 */
	protected static Map<String, MessageFormat> messageFormats = new HashMap<String, MessageFormat>();

	public final static BigDecimal kibibyte = new BigDecimal(1024); // binary

	// 2^10
	public final static String kb = "KiB";

	public final static BigDecimal mebibyte = new BigDecimal(1048576); // binary

	// 2^20
	public final static String mb = "MiB";

	private static int decimal = 2;

	/**
	 * Protected no-args constructor.
	 */
	protected Translator() {
	}

	/**
	 * This method returns the resource bundle for the provided base name and
	 * locale.
	 * 
	 * @param baseName
	 *            the base name (must be relative to BUNDLE_DIRECTORY)
	 * @param locale
	 *            the locale
	 * @return Resource bundle for base name and locale
	 */
	protected static ResourceBundle getBundle(String baseName, Locale locale) {
		if (ResourceBundleManager.isEmptyBundle()) {
			// list all files
			File[] listOfFiles = new File("resources").listFiles();
			Set<String> messageFiles = new TreeSet<String>();

			// check for message files
			Pattern pattern = Pattern
					.compile("[a-z]*-message((_)*[a-z-A-Z])*.properties");
			int index = -1;
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
					Matcher matcher = pattern.matcher(listOfFiles[i].getName());
					if (matcher.matches()) {
						index = listOfFiles[i].getName().indexOf("_");
						if (index < 0) {
							index = listOfFiles[i].getName().indexOf(".");
						}
						if (index > -1) {
							messageFiles.add(listOfFiles[i].getName()
									.substring(0, index));
						}
					}
				}
			}

			// add message files to bundle
			Log log = LogFactory.getLog(LogType.SYSTEM, Translator.class);
			for (String messageFileName : messageFiles) {
				try {
					ResourceBundleManager.addResourceBundle(ResourceBundle
							.getBundle(ResourceBundleManager.BUNDLE_DIRECTORY
									+ messageFileName, getLocale()));
					logAddedResourceBundle(log, messageFileName);
				} catch (Exception e) {
					try {
						ResourceBundleManager.ResourceBundleManagerImpl
								.addResourceBundleFromProperties(
										ResourceBundleManager.BUNDLE_DIRECTORY
												+ messageFileName, locale);
						logAddedResourceBundle(log, messageFileName);
					} catch (Exception e1) {
						Log eLog = LogFactory.getLog(LogType.EXCEPTION,
								Translator.class);
						eLog.error("Error loading bundle: "
								+ ResourceBundleManager.BUNDLE_DIRECTORY
								+ messageFileName + ", locale: " + locale, e1);
					}
				}
			}
		}
		if (baseName != null) {
			return ResourceBundleManager.getBundle(baseName);
		} else {
			return ResourceBundleManager.getBundle();
		}
	}

	private static void logAddedResourceBundle(Log log, String messageFileName) {
		if (log.isDebugEnabled()) {
			log.debug("Adding resource bundle: "
					+ ResourceBundleManager.BUNDLE_DIRECTORY + messageFileName);
		}
	}

	/**
	 * Escapes any single quote characters that are included in the specified
	 * string.
	 * 
	 * @param string
	 *            The string to be escaped
	 * @return the escaped string
	 */
	protected static String escape(String string) {
		if ((string == null) || (string.indexOf('\'') < 0)) {
			return string;
		}

		int n = string.length();
		StringBuffer sb = new StringBuffer(n);

		for (int i = 0; i < n; i++) {
			char ch = string.charAt(i);

			if (ch == '\'') {
				sb.append('\'');
			}

			sb.append(ch);
		}

		return (sb.toString());
	}

	/**
	 * Retrieve the local from configuration if fails default locale
	 * 
	 * @return locale
	 */
	public static Locale getLocale() {
		// language lowercase two-letter ISO-639 code
		String language = Config.get(Config.LOCALE_LANGUAGE);

		// country uppercase two-letter ISO-3166 code
		String country = Config.get(Config.LOCALE_COUNTRY);

		if (language.equals("") || country.equals("")) {
			return Locale.getDefault();
		} else {
			try {
				// http://java.sun.com/javase/6/docs/technotes/guides/intl/locale.doc.html
				return new Locale(language, country);
			} catch (Exception e) {
				LogFactory.getLog(LogType.EXCEPTION, Translator.class).error(
						"Could not construct local based on, language: "
								+ language + " and country: " + country, e);
				return Locale.getDefault();
			}
		}
	}

	/**
	 * Retrieve the country code from the locale
	 * 
	 * @return country code defined by ISO-639-2, if the 2 letter code is void
	 *         it defaults to the 3 letter definition of language codes defined
	 *         in ISO-639-3
	 */
	public static String getLocaleLanguage() {
		Locale locale = getLocale();
		String language = locale.getLanguage();
		if (language == null || language.equals("")) {
			language = locale.getLanguage();
		}
		return language;
	}

	/**
	 * Retrieve time zone from configuration
	 * 
	 * @return time zone
	 */
	public static TimeZone getTimeZone() {
		String timeZoneString = Config.get(Config.TIME_ZONE);
		if (timeZoneString.equals("")) {
			return TimeZone.getDefault();
		} else {
			TimeZone.setDefault(TimeZone.getTimeZone(timeZoneString));
			return TimeZone.getDefault();
		}
	}

	/**
	 * This method returns the localized string for the resource bundle
	 * specified by base name using the provided key and locale.
	 * 
	 * @param locale
	 *            the locale
	 * @param baseName
	 *            the base name
	 * @param key
	 *            the key
	 * @return String localized string
	 */
	private static String transInternal(Locale locale, String baseName,
			String key) {
		String string = null;

		try {
			string = getBundle(baseName, locale).getString(key);
		} catch (MissingResourceException ex) {
			Log log = LogFactory.getLog(LogType.EXCEPTION, Translator.class);
			log.warn(ex.getMessage());
			string = key;
		}

		return string;
	}

	/**
	 * This method returns the localized string for the resource bundle
	 * specified by base name using the provided key and locale. The localized
	 * string is afterwards formatted according to the provided arguments.
	 * 
	 * @param locale
	 *            the locale
	 * @param baseName
	 *            the base name
	 * @param key
	 *            the key
	 * @param args
	 *            arguments for formatting
	 * @return String localized and formatted string
	 */
	public static String trans(Locale locale, String baseName, String key,
			Object[] args) {
		String string = transInternal(locale, baseName, key);

		if (args != null) {
			MessageFormat formatter = null;

			synchronized (messageFormats) {
				formatter = (MessageFormat) messageFormats.get(key);

				if (formatter == null) {
					formatter = new MessageFormat(escape(string));
					messageFormats.put(key, formatter);
				}
			}

			string = formatter.format(args);
		}

		return string;
	}

	/**
	 * @see trans(Locale, String, Object[]).
	 * @param locale
	 *            the locale
	 * @param key
	 *            the key
	 * @param arg0
	 *            the arg0
	 * @return String localized string
	 */
	public static String trans(Locale locale, String key, Object arg0) {
		return trans(locale, key, new Object[] { arg0 });
	}

	/**
	 * @see trans(Locale, String, String).
	 * @param locale
	 *            the locale
	 * @param key
	 *            the key
	 * @return String localized string
	 */
	public static String trans(Locale locale, String key) {
		return transInternal(locale, DEFAULT_BUNDLE, key);
	}

	/**
	 * @see trans(Locale, String, String, Object[]).
	 * @param locale
	 *            the locale
	 * @param key
	 *            the key
	 * @param args
	 *            arguments for formatting
	 * @return String localized string
	 */
	public static String trans(Locale locale, String key, Object[] args) {
		return trans(locale, DEFAULT_BUNDLE, key, args);
	}

	/**
	 * @see trans(Locale, String, String). the session context
	 * @param key
	 *            the key
	 * @return String localized string
	 */
	public static String trans(String key) {
		return transInternal(getLocale(), DEFAULT_BUNDLE, key);
	}

	/**
	 * @see trans(Locale, String, String, Object[]).
	 * @param key
	 *            the key
	 * @param args
	 *            arguments for formatting
	 * @return String localized and formatted string
	 */
	public static String trans(String key, Object[] args) {
		return trans(getLocale(), DEFAULT_BUNDLE, key, args);
	}

	/**
	 * @see trans(String, Object[]).
	 * @param key
	 *            the key
	 * @param arg0
	 *            the arg0
	 * @return String
	 */
	public static String trans(String key, Object arg0) {
		return trans(key, new Object[] { arg0 });
	}

	/**
	 * @see trans(String, Object[]).
	 * @param key
	 *            the key
	 * @param arg0
	 *            the arg0
	 * @param arg1
	 *            the arg1
	 * @return String
	 */
	public static String trans(String key, Object arg0, Object arg1) {
		return trans(key, new Object[] { arg0, arg1 });
	}

	/**
	 * @see trans(String, Object[]).
	 * @param key
	 *            the key
	 * @param arg0
	 *            the arg0
	 * @param arg1
	 *            the arg1
	 * @param arg2
	 *            the arg2
	 * @return String
	 */
	public static String trans(String key, Object arg0, Object arg1, Object arg2) {
		return trans(key, new Object[] { arg0, arg1, arg2 });
	}

	public static String transLang(String langCode) {
		String nill = "";
		if (langCode == null || langCode.equals(nill)) {
			return nill;
		}
		return getLocale().getDisplayLanguage(new Locale(langCode));
	}

	/**
	 * Format a timestamp to readable string
	 * 
	 * @param timestamp
	 *            to format
	 * @return formatted timestamp
	 */
	public static synchronized String format(Timestamp timestamp) {
		SimpleDateFormat simpleTimestampFormat = new SimpleDateFormat(
				Config.get(Config.FORMAT_TIMESTAMP));
		return simpleTimestampFormat.format(timestamp);
	}

	/**
	 * Format a date to a readable string
	 * 
	 * @param date
	 *            to format
	 * @return formatted date
	 */
	public static synchronized String format(Date date) {
		SimpleDateFormat simpleTimestampFormat = new SimpleDateFormat(
				Config.get(Config.FORMAT_DATE));
		return simpleTimestampFormat.format(date);
	}

	/**
	 * Format a date string to a calendar
	 * 
	 * @param dateString
	 *            of the date format set
	 * @return calendar
	 */
	public static synchronized Calendar formatDate(String dateString)
			throws DDIFtpException {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				Config.get(Config.FORMAT_DATE));
		try {
			java.util.Date date = dateFormat.parse(dateString);
			Calendar result = Calendar.getInstance();
			result.setTimeZone(getTimeZone());
			result.setTimeInMillis(date.getTime());
			return result;
		} catch (Exception e) {
			throw new DDIFtpException(
					"translate.timeformat.error",
					new Object[] { dateString, Config.get(Config.FORMAT_DATE) },
					e);
		}
	}

	/**
	 * Format a timestamp string to a calendar
	 * 
	 * @param timestampString
	 *            of the date format set
	 * @return calendar
	 */
	public static synchronized Calendar formatTimestamp(String timestampString)
			throws DDIFtpException {
		SimpleDateFormat simpleTimestampFormat = new SimpleDateFormat(
				Config.get(Config.FORMAT_TIMESTAMP));
		try {
			java.util.Date date = simpleTimestampFormat.parse(timestampString);
			Calendar result = Calendar.getInstance();
			result.setTimeZone(getTimeZone());
			result.setTimeInMillis(date.getTime());
			return result;
		} catch (Exception e) {
			throw new DDIFtpException("translate.timeformat.error",
					new Object[] { timestampString,
							Config.get(Config.FORMAT_TIMESTAMP) }, e);
		}
	}

	/**
	 * Format an ISO8601 time string defined by into a Calendar<BR>
	 * Defined by '-'? yyyy '-' mm '-' dd 'T' hh ':' mm ':' ss ('.' s+)?
	 * (zzzzzz)?
	 * 
	 * @see http://www.w3.org/TR/xmlschema-2/#dateTime
	 * @param time
	 *            string
	 * @return calendar
	 * @throws DDIFtpException
	 */
	public static Calendar formatIso8601DateTime(String time)
			throws DDIFtpException {
		// yyyy-MM-dd'T'HH:mm:ss.SSSZZ full format
		DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
		try {
			DateTime dateTime = fmt.parseDateTime(time);
			return dateTime.toCalendar(getLocale());
		} catch (IllegalArgumentException e) {
			try {
				// yyyy-MM-dd'T'HH:mm:ssZZ with out millisecond
				fmt = ISODateTimeFormat.dateTimeNoMillis();
				fmt.withLocale(getLocale());
				fmt.withZone(DateTimeZone.forTimeZone(getTimeZone()));
				DateTime dateTime = fmt.parseDateTime(time);
				return dateTime.toCalendar(getLocale());
			} catch (IllegalArgumentException e1) {
				try {
					// yyyy-MM-dd'T'HH:mm:ss.SS with out time zone
					fmt = ISODateTimeFormat.dateHourMinuteSecondFraction();
					fmt.withLocale(getLocale());
					fmt.withZone(DateTimeZone.forTimeZone(getTimeZone()));
					DateTime dateTime = fmt.parseDateTime(time);
					return dateTime.toCalendar(getLocale());
				} catch (Exception e2) {
					try {
						// yyyy-MM-dd'T'HH:mm:ss with out millisecond and time
						// zone
						fmt = ISODateTimeFormat.dateHourMinuteSecond();
						fmt.withLocale(getLocale());
						fmt.withZone(DateTimeZone.forTimeZone(getTimeZone()));
						DateTime dateTime = fmt.parseDateTime(time);
						return dateTime.toCalendar(getLocale());
					} catch (IllegalArgumentException e3) {
						try {
							fmt = ISODateTimeFormat.dateParser();
							fmt.withLocale(getLocale());
							fmt.withZone(DateTimeZone
									.forTimeZone(getTimeZone()));
							DateTime dateTime = fmt.parseDateTime(time);
							return dateTime.toCalendar(getLocale());
						} catch (Exception e4) {
							throw new DDIFtpException(
									"translate.timeformat.error",
									new Object[] { time,
											"'-'? yyyy '-' mm '-' dd 'T' hh ':' mm ':' ss ('.' s+)? (zzzzzz)?" },
									e);
						}
					}
				}
			}
		}
	}

	/**
	 * Format time in milli seconds to an ISO8601 time string<BR>
	 * Defined by '-'? yyyy '-' mm '-' dd 'T' hh ':' mm ':' ss ('.' s+)?
	 * (zzzzzz)?
	 * 
	 * @see http://www.w3.org/TR/xmlschema-2/#dateTime
	 * @param time
	 *            in milli seconds
	 * @return ISO8601 time string
	 * @throws DDIFtpException
	 */
	public static String formatIso8601DateTime(long time) {
		DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
		fmt.withLocale(getLocale());
		fmt.withZone(DateTimeZone.forTimeZone(getTimeZone()));
		return fmt.print(time);
	}

	/**
	 * Creates a ISO8601 duration of the form PyYmMwWdDThHmMsS
	 * 
	 * @param start
	 * @param end
	 * @return ISO8601 duration
	 * @throws DDIFtpException
	 */
	public static String formatIso8601Duration(long start, long end)
			throws DDIFtpException {
		DateTime startTime = new DateTime(start);
		DateTime endTime = new DateTime(end);
		DurationFieldType[] types = { DurationFieldType.years(),
				DurationFieldType.months(), DurationFieldType.days(),
				DurationFieldType.hours(), DurationFieldType.minutes(),
				DurationFieldType.seconds() };
		PeriodType periodType = PeriodType.forFields(types);

		try {
			Period period = new Interval(startTime, endTime)
					.toPeriod(periodType);
			PeriodFormatter isoPeriodFormat = ISOPeriodFormat.standard();
			isoPeriodFormat.withLocale(getLocale());
			return period.toString(isoPeriodFormat);
		} catch (Exception e) {
			throw new DDIFtpException("translate.period.intevalerror",
					new Object[] { start, end }, e);
		}
	}

	/**
	 * Format bytes to human readable format of kibibytes and medibytes
	 * 
	 * @param bytes
	 * @return human readable byte format
	 */
	public static String formatBytes(Long bytes) {
		if (bytes > kibibyte.longValue()) {
			return bytesToKiloBytes(bytes);
		} else {
			return bytes + " B";
		}
	}

	private static String bytesToKiloBytes(Long bytes) {
		if (bytes > mebibyte.longValue()) {
			return bytesToMegaBytes(bytes);
		} else {
			return new BigDecimal(bytes).divide(kibibyte, decimal,
					RoundingMode.HALF_EVEN) + " " + kb;
		}
	}

	private static String bytesToMegaBytes(Long bytes) {
		return new BigDecimal(bytes).divide(mebibyte, decimal,
				RoundingMode.HALF_EVEN) + " " + mb;
	}

	public static BigInteger stringToBigInt(String value)
			throws DDIFtpException {
		if (value.equals("")) { // guard
			return new BigInteger("0");
		}

		BigInteger bigint = null;
		try {
			bigint = new BigInteger(value);
		} catch (Exception e) {
			throw new DDIFtpException(trans("bigint.error",
					new Object[] { value }));
		}
		return bigint;
	}
}
