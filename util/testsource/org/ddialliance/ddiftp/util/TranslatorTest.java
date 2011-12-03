package org.ddialliance.ddiftp.util;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;

import junit.framework.Assert;

import org.joda.time.DateTime;
import org.junit.Test;

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

public class TranslatorTest {
	static String iso8601Time = "2008-12-31T00:00:00.000";
	static String time = "2008-12-31 00:00";
	static String date = "2008-12-31";
	static Calendar calendar = Calendar.getInstance(Translator.getLocale());
	static long timeLong = 1230678000000L;
	static Timestamp timestamp;
	static {
		calendar.setTimeInMillis(timeLong);
		timestamp = new Timestamp(timeLong);

		// compute offset to conform to local
		int offsetMillis = (calendar.getTimeZone().getOffset(timeLong)) / 3600000;
		String offset = "" + offsetMillis;
		if (offsetMillis < 0 && offsetMillis > -10) {
			offset = offset.replace("-", "");
			iso8601Time += ("-" + "0" + offset + ":00");
		} else if (offsetMillis < -9) {
			iso8601Time += (offset + ":00");
		} else if (offsetMillis > 0 && offsetMillis < 10) {
			iso8601Time += ("+0" + offset + ":00");
		} else if (offsetMillis > 9) {
			iso8601Time += ("+" + offset + ":00");
		} else if (offsetMillis == 0) {
			iso8601Time += "Z";
		}
		// System.out.println(iso8601Time);
	}

	@Test
	public void formatTimestamp() throws Exception {
		String result = Translator.format(timestamp);
		Assert.assertEquals(time, result);
	}

	@Test
	public void formatDate() throws Exception {
		Date dateDate = new Date(calendar.getTimeInMillis());
		String result = Translator.format(dateDate);
		Assert.assertEquals(date, result);
	}

	@Test
	public void formatDateToCalendar() throws Exception {
		Calendar result = Translator.formatDate(date);
		Assert.assertEquals(calendar, result);
	}

	@Test
	public void formatTimestampToCalendar() throws Exception {
		Calendar result = Translator.formatTimestamp(time);
		Assert.assertEquals(calendar, result);
	}

	@Test
	public void formatIso8601DateTime() throws Exception {
		Calendar result = Translator.formatIso8601DateTime(iso8601Time);
		Assert.assertEquals(calendar, result);

		String iso8601Time = "2008-12-31T00:00:00+01:00";
		result = Translator.formatIso8601DateTime(iso8601Time);
		Assert.assertEquals(calendar, result);

		iso8601Time = "2008-12-31T00:00:00";
		result = Translator.formatIso8601DateTime(iso8601Time);
		Assert.assertEquals(calendar, result);

		iso8601Time = "2008-12-31T00:00:00.000";
		result = Translator.formatIso8601DateTime(iso8601Time);
		Assert.assertEquals(calendar, result);

		iso8601Time = "2008-12-20";
		result = Translator.formatIso8601DateTime(iso8601Time);
		System.out.println(result.getTime());

		iso8601Time = "2008-12";
		result = Translator.formatIso8601DateTime(iso8601Time);
	}

	@Test
	public void formatIso8601DateTimeStr() throws Exception {
		String result = Translator.formatIso8601DateTime(calendar
				.getTimeInMillis());
		Assert.assertEquals(iso8601Time, result);
	}

	@Test
	public void formatIso8601Duration() throws Exception {
		DateTime start = new DateTime(2000, 2, 7, 7, 40, 20, 500);
		DateTime end = new DateTime(2008, 7, 4, 15, 30, 45, 100);
		Assert.assertEquals(
				"P8Y4M27DT7H50M24S",
				Translator.formatIso8601Duration(start.getMillis(),
						end.getMillis()));
	}

	@Test
	public void getLanguage() throws Exception {
		System.out.println(Translator.getLocaleLanguage());
	}
}
