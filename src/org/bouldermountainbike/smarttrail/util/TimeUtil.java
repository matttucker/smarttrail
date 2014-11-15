/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */
package org.bouldermountainbike.smarttrail.util;

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.content.Context;

/**
 * 
 * @author matt@geozen.com
 * 
 */

public class TimeUtil {

	public static final long SEC_MS = 1000L;
	public static final long MIN_MS = 60L * SEC_MS;
	public static final long HOUR_MS = 60L * MIN_MS;
	public static final long DAY_MS = 24L * HOUR_MS;

	public static String ago(long time, long delta) {
	
		boolean inFuture = false;
		StringBuilder sb = new StringBuilder();
		
		if (delta < 0) {
			delta *= -1L;
			inFuture = true;
			// hack for bma app as server time could be off from app.
			return "now";
		}
		if (delta > (1000L * 60L * 60L * 24L * 10000L)) {
			return "a long time ago";
		}
		long seconds = delta / 1000L;

		if (seconds < 60L)
			sb.append(seconds).append(" seconds ");
		else {
			long minutes = delta / (1000L * 60L);

			if (minutes < 60L)
				if (minutes == 1)
					sb.append(minutes).append(" minute ");
				else
					sb.append(minutes).append(" minutes ");
			else {
				long hours = delta / (1000L * 60L * 60L);
				if (hours < 24L)
					if (hours == 1L) {
						sb.append(hours).append(" hour ");
					} else {
						sb.append(hours).append(" hours ");
					}
				else {
					long days = delta / (1000L * 60L * 60L * 24L);
					if (days < 7L) {
						if (days == 1L) {
							sb.append(days).append(" day ");
						} else {
							sb.append(days).append(" days ");
						}
					} else {
						long weeks = delta / (1000L * 60L * 60L * 24L * 7L);
						if (weeks < 3L) {
							if (weeks == 1L) {
								sb.append(weeks).append(" week ");
							} else {
								sb.append(weeks).append(" weeks ");
							}
						} else {
							return getDate(time);
						}
					}

				}

			}
		}
		if (inFuture) {
			sb.append("from now");
		} else {
			sb.append("ago");
		}
		return sb.toString();
	}

	public static String eventFormat(long start, long end) {
		Date startDate = new Date(start);
		Date endDate = new Date(end);
		SimpleDateFormat formatter1 = new SimpleDateFormat("EEEE, MMMM d",
				Locale.US);
		formatter1.format(startDate);
		SimpleDateFormat formatter2 = new SimpleDateFormat("h:mma", Locale.US);
		return formatter2.format(startDate) + "-" + formatter2.format(endDate)
				+ " " + formatter1.format(startDate);
	}

	/**
	 * Return date in specified format.
	 * 
	 * @param milliSeconds
	 *            Date in milliseconds
	 * @param dateFormat
	 *            Date format
	 * @return String representing date in specified format
	 */
	public static String getDate(long milliSeconds) {
		// Format dateFormat = android.text.format.DateFormat
		// .getDateFormat(context);
		// String pattern = ((SimpleDateFormat)
		// dateFormat).toLocalizedPattern();
		// // Create a DateFormatter object for displaying date in specified
		// // format.
		DateFormat formatter = DateFormat.getDateInstance();

		// Create a calendar object that will convert the date and time value in
		// milliseconds to date.
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(milliSeconds);
		return formatter.format(calendar.getTime());
	}
}
