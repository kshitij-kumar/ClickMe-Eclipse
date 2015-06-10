package com.kshitij.android.clickme.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.text.format.DateUtils;

/**
 * Created by kshitij.kumar on 09-06-2015.
 */

/**
 * A helper class to generate time strings like facebook application
 * 
 */

public class TimeFormatter {

	private static final String TAG = TimeFormatter.class.getSimpleName();

	public static String getCustomisedTimeLabel(long longDate) {
		Date postDate = new Date(longDate);
		String label = "";

		if (longDate < System.currentTimeMillis()) {

			if (DateUtils.isToday(longDate)) {
				label = DateUtils
						.getRelativeTimeSpanString(postDate.getTime(),
								(System.currentTimeMillis()),
								DateUtils.SECOND_IN_MILLIS,
								DateUtils.FORMAT_ABBREV_ALL).toString();
			} else if (isYesterday(longDate)) {
				label = "Yesterday";
			} else {
				label = TimeFormatter.getDateFormatMddYYYYhhmma(longDate);
			}
			return label;
		} else {
			// find the time span for tommorow
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd",
					Locale.ENGLISH);
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, 1);
			@SuppressWarnings("deprecation")
			Date date = new Date(dateFormat.format(cal.getTime()));

			// compare feed timespan with tommorow's timespan
			if ((longDate - System.currentTimeMillis()) <= date.getTime()
					- System.currentTimeMillis()) {
				return DateUtils
						.getRelativeTimeSpanString(postDate.getTime(),
								(System.currentTimeMillis()),
								DateUtils.SECOND_IN_MILLIS,
								DateUtils.FORMAT_ABBREV_ALL).toString();
			} else {
				return TimeFormatter.getDateFormatMddYYYYhhmma(longDate);
			}

		}

	}

	public static boolean isYesterday(long when) {

		Calendar cal = Calendar.getInstance(Locale.ENGLISH);
		cal.setTimeInMillis(System.currentTimeMillis());
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		int sec = cal.get(Calendar.SECOND);
		int millisec = cal.get(Calendar.MILLISECOND);
		long millisTillYesterday = millisec + (sec * 1000) + (min * 60 * 1000)
				+ (hour * 60 * 60 * 1000);
		long yesterdayEnd = System.currentTimeMillis() - millisTillYesterday
				- (60 * 1000);
		long yesterdayStart = System.currentTimeMillis() - millisTillYesterday
				- (24 * 60 * 60 * 1000);

		if ((when >= yesterdayStart) && (when < yesterdayEnd))
			return true;
		else
			return false;
	}

	public static String getDateFormatMddYYYYhhmma(long longDate) {
		SimpleDateFormat dateformat = new SimpleDateFormat(
				"MMM dd, yyyy hh:mm a", Locale.ENGLISH);
		return dateformat.format(new Date(longDate));
	}

}
