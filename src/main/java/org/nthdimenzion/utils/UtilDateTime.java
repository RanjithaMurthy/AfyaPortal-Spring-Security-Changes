package org.nthdimenzion.utils;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import javax.xml.datatype.XMLGregorianCalendar;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Utility class for handling java.util.Date, the java.sql data/time classes and
 * related
 */
public class UtilDateTime {
	public static final String[] months = { "January", "February", "March", "April", "May", "June", "July", "August",
			"September", "October", "November", "December" };
	public static final String DEFAULT_TO_STRING_DATEFORMAT = "EEE MMM dd HH:mm:ss z yyyy"; // Wed
	// Jul
	// 21
	// 00:00:00
	// IST
	// 2010

	public static final String[] DAYS = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };

	public static final String[] DAYSINSHORT = { "SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT" };

	public static final String[][] timevals = { { "1000", "millisecond" }, { "60", "second" }, { "60", "minute" },
			{ "24", "hour" }, { "168", "week" } };

	public static final DecimalFormat df = new DecimalFormat("0.00;-0.00");
	/**
	 * JDBC escape format for Date conversions.
	 */
	public static final String DATE_FORMAT = "yyyy-MM-dd";

	/**
	 * JDBC escape format for Date conversions.
	 */
	public static final String DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
	/**
	 * JDBC escape format for Time conversions.
	 */
	public static final String DATE_TIMESTAMP_FORMAT = "dd/MM/yyyy HH:mm";

	public static final String TIME_FORMAT = "HH:mm:ss";

	public static final String AM_PM_FORMAT = "hh:mm a";

	public static SimpleDateFormat AM_PM_FORMATTER = new SimpleDateFormat(AM_PM_FORMAT);

	public static SimpleDateFormat AM_PM = new SimpleDateFormat("HHmm");

	public static SimpleDateFormat MM_DD_YYYY = new SimpleDateFormat("dd-MM-yyyy");

	public static SimpleDateFormat HIPHEN_DATE_FORMATTER = new SimpleDateFormat(DATE_FORMAT);

	public static SimpleDateFormat MONTH_DATE_FORMATTER = new SimpleDateFormat("MMM, dd");

	public static SimpleDateFormat SLASH_DATE_FORMATTER = new SimpleDateFormat("yyyy/MM/dd");

	public static SimpleDateFormat DEFAULT_DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy");

	public static SimpleDateFormat FULL_DATE_FORMATTER = new SimpleDateFormat("MMM,dd,yyyy");
	
	public static final String DATE_TIME_FORMATS = "dd-MM-yyyy HH:mm:ss a";
	
	public static int compareByMonths(Date from, Date thru, int months){
	int yearDiff = thru.getYear() - from.getYear();  
	int monthDiff = thru.getMonth() - from.getMonth();
	int dayDiff = thru.getDate() - from.getDate(); 
	if (monthDiff < 0) {
		yearDiff--;
		monthDiff = monthDiff + 12;
	}
	monthDiff = (yearDiff * 12) + monthDiff;  
	if(monthDiff < 0)
		return 1;
	if(monthDiff == 0)
		return dayDiff == 0 ? 0 : (dayDiff > 0 ? 1 : -1);
	return (monthDiff - months) > 0 ? 1 : -1;
	}

	public static int getIntervalInMonths(Date from, Date thru) {
	int yearDiff = thru.getYear() - from.getYear();
	int monthDiff = thru.getMonth() - from.getMonth();
	if (monthDiff < 0) {
		yearDiff--;
		monthDiff = monthDiff + 12;
	}
	return (yearDiff * 12) + monthDiff;
	}

	public static int getIntervalInYears(Date from, Date thru) {
	return getIntervalInMonths(from, thru) / 12;
	}

	public static String getIntervalInYearsAndMonths(Date from, Date thru) {
	int monthDiff = getIntervalInMonths(from, thru);
	return (monthDiff / 12) + " Year(s)" + ", " + (monthDiff % 12) + " Month(s)";
	}

	public static long getIntervalInHours(Date from, Date thru) {
	long deltaInMillis = thru.getTime() - from.getTime();
	return deltaInMillis / (60 * 60 * 1000);
	}

	public static int getIntervalInDays(Date from, Date thru) {
	return thru != null ? (int) ((getDayEnd(thru).getTime() - getDayStart(from).getTime()) / (24 * 60 * 60 * 1000)) : 0;
	}

	public static int getIntervalInWeeks(Date from, Date thru) {
	return getIntervalInDays(from, thru) / 7;
	}

	public static Date addDaysToDate(Date start, int days) {
	return new Date(start.getTime() + (24L * 60L * 60L * 1000L * days));
	}

	public static Date addDaysToDate(Date start, Double days) {
	return new Date(start.getTime() + ((int) (24L * 60L * 60L * 1000L * days)));
	}

	public static double getInterval(Date from, Date thru) {
	return thru != null ? thru.getTime() - from.getTime() : 0;
	}

	public static Date getDate(String milliSecs) throws NumberFormatException {
	return new Date(Long.parseLong(milliSecs));
	}

	public static Date addMonthsToDate(Date start, int months) {
	GregorianCalendar calendar = new GregorianCalendar();
	calendar.setTime(start);
	calendar.add(Calendar.MONTH, months);
	return calendar.getTime();
	}

	public static Date addYearsToDate(Date start, int years) {
	GregorianCalendar calendar = new GregorianCalendar();
	calendar.setTime(start);
	calendar.add(Calendar.YEAR, years);
	return calendar.getTime();
	}

	/**
	 * Returns currentTimeMillis as String
	 * 
	 * @return String(currentTimeMillis)
	 */
	public static String nowAsString() {
	return Long.toString(System.currentTimeMillis());
	}

	/**
	 * Return a string formatted as yyyyMMddHHmmss
	 * 
	 * @return String formatted for right now
	 */
	public static String nowDateString() {
	return nowDateString("yyyyMMddHHmmss");
	}

	/**
	 * Return a string formatted as format
	 * 
	 * @return String formatted for right now
	 */
	public static String nowDateString(String format) {
	SimpleDateFormat df = new SimpleDateFormat(format);
	return df.format(new Date());
	}

	/**
	 * Return a Date for right now
	 * 
	 * @return Date for right now
	 */
	public static Date nowDate() {
	return new Date();
	}

	public static Date getDayStart(Date stamp) {
	return getDayStart(stamp, 0);
	}

	public static Date getDayStart(Date stamp, int daysLater) {
	return getDayStart(stamp, daysLater, TimeZone.getDefault(), Locale.getDefault());
	}

	public static Date getNextDayStart(Date stamp) {
	return getDayStart(stamp, 1);
	}

	public static Date getDayEnd(Date stamp) {
	return getDayEnd(stamp, Long.valueOf(0));
	}

	public static Date getDayEnd(Date stamp, Long daysLater) {
	return getDayEnd(stamp, daysLater, TimeZone.getDefault(), Locale.getDefault());
	}

	/**
	 * Return the date for the first day of the year
	 *
	 * @param stamp
	 * @return Date
	 */
	public static Date getYearStart(Date stamp) {
	return getYearStart(stamp, 0, 0, 0);
	}

	public static Date getYearStart(Date stamp, int daysLater) {
	return getYearStart(stamp, daysLater, 0, 0);
	}

	public static Date getYearStart(Date stamp, int daysLater, int yearsLater) {
	return getYearStart(stamp, daysLater, 0, yearsLater);
	}

	public static Date getYearStart(Date stamp, int daysLater, int monthsLater, int yearsLater) {
	return getYearStart(stamp, daysLater, monthsLater, yearsLater, TimeZone.getDefault(), Locale.getDefault());
	}

	public static Date getYearStart(Date stamp, Number daysLater, Number monthsLater, Number yearsLater) {
	return getYearStart(stamp, (daysLater == null ? 0 : daysLater.intValue()), (monthsLater == null ? 0 : monthsLater
			.intValue()), (yearsLater == null ? 0 : yearsLater.intValue()));
	}

	/**
	 * Return the date for the first day of the month
	 *
	 * @param stamp
	 * @return Date
	 */
	public static Date getMonthStart(Date stamp) {
	return getMonthStart(stamp, 0, 0);
	}

	public static Date getMonthStart(Date stamp, int daysLater) {
	return getMonthStart(stamp, daysLater, 0);
	}

	public static Date getMonthStart(Date stamp, int daysLater, int monthsLater) {
	return getMonthStart(stamp, daysLater, monthsLater, TimeZone.getDefault(), Locale.getDefault());
	}

	/**
	 * Return the date for the first day of the week
	 *
	 * @param stamp
	 * @return Date
	 */
	public static Date getWeekStart(Date stamp) {
	return getWeekStart(stamp, 0, 0);
	}

	public static Date getWeekStart(Date stamp, int daysLater) {
	return getWeekStart(stamp, daysLater, 0);
	}

	public static Date getWeekStart(Date stamp, int daysLater, int weeksLater) {
	return getWeekStart(stamp, daysLater, weeksLater, TimeZone.getDefault(), Locale.getDefault());
	}

	public static Date getWeekEnd(Date stamp) {
	return getWeekEnd(stamp, TimeZone.getDefault(), Locale.getDefault());
	}

	public static Calendar toCalendar(Date stamp) {
	Calendar cal = Calendar.getInstance();
	if (stamp != null) {
		cal.setTimeInMillis(stamp.getTime());
	}
	return cal;
	}

	/**
	 * Converts a date String into a Date
	 *
	 * @param date
	 *            The date String: MM/DD/YYYY
	 * @return A Date made from the date String
	 */
	public static Date toSqlDate(String date) {
	Date newDate = toDate(date, "00:00:00");

	if (newDate != null) {
		return new Date(newDate.getTime());
	} else {
		return null;
	}
	}

	/**
	 * Makes a Date from separate Strings for month, day, year
	 *
	 * @param monthStr
	 *            The month String
	 * @param dayStr
	 *            The day String
	 * @param yearStr
	 *            The year String
	 * @return A Date made from separate Strings for month, day, year
	 */
	public static Date toSqlDate(String monthStr, String dayStr, String yearStr) {
	Date newDate = toDate(monthStr, dayStr, yearStr, "0", "0", "0");

	if (newDate != null) {
		return new Date(newDate.getTime());
	} else {
		return null;
	}
	}

	public static Date toDate(String monthStr, String dayStr, String yearStr, String hourStr, String minuteStr,
			String secondStr) {
	Date newDate = new Date(Integer.parseInt(yearStr), Integer.parseInt(monthStr), Integer.parseInt(dayStr),
			Integer.parseInt(hourStr), Integer.parseInt(minuteStr), Integer.parseInt(secondStr));
	if (newDate != null) {
		return new Date(newDate.getTime());
	} else {
		return null;
	}
	}

	public static Date toDate(Date date) {
	if (date == null) return null;
	return new Date(date.getTime());
	}

	/**
	 * Converts a date and time String into a Date
	 *
	 * @param dateTime
	 *            A combined data and time string in the format
	 *            "MM/DD/YYYY HH:MM:SS", the seconds are optional
	 * @return The corresponding Date
	 */
	public static Date toDate(String dateTime) {
	if (dateTime == null) {
		return null;
	}
	// dateTime must have one space between the date and time...
	String date = dateTime.substring(0, dateTime.indexOf(" "));
	String time = dateTime.substring(dateTime.indexOf(" ") + 1);
	return toDate(date, time);
	}

	/**
	 * Converts a date String and a time String into a Date
	 *
	 * @param date
	 *            The date String: MM/DD/YYYY
	 * @param time
	 *            The time String: either HH:MM or HH:MM:SS
	 * @return A Date made from the date and time Strings
	 */
	public static Date toDate(String date, String time) {
	if (date == null || time == null) return null;
	String month;
	String day;
	String year;
	String hour;
	String minute;
	String second;

	int dateSlash1 = date.indexOf("/");
	int dateSlash2 = date.lastIndexOf("/");

	if (dateSlash1 <= 0 || dateSlash1 == dateSlash2) return null;
	int timeColon1 = time.indexOf(":");
	int timeColon2 = time.lastIndexOf(":");

	if (timeColon1 <= 0) return null;
	month = date.substring(0, dateSlash1);
	day = date.substring(dateSlash1 + 1, dateSlash2);
	year = date.substring(dateSlash2 + 1);
	hour = time.substring(0, timeColon1);

	if (timeColon1 == timeColon2) {
		minute = time.substring(timeColon1 + 1);
		second = "0";
	} else {
		minute = time.substring(timeColon1 + 1, timeColon2);
		second = time.substring(timeColon2 + 1);
	}

	return toDate(month, day, year, hour, minute, second);
	}

	public static String toDateString(Date date, String format) {
	if (date == null) return "";
	SimpleDateFormat dateFormat = null;
	if (format != null) {
		dateFormat = new SimpleDateFormat(format);
	} else {
		dateFormat = new SimpleDateFormat();
	}

	Calendar calendar = Calendar.getInstance();

	calendar.setTime(date);
	return dateFormat.format(date);
	}

	public static String format(Date date) {
	if (date != null) return format(date, DEFAULT_DATE_FORMATTER);
	return null;
	}

	public static String formatToDbDate(Date date) {
		if (date != null) return format(date, HIPHEN_DATE_FORMATTER);
		return null;
	}

	public static String format(Date date, Date time) {
	StringBuilder builder = new StringBuilder();
	if (date != null) builder.append(format(date));
	if (time != null)
		builder.append(Constants.BLANK_CHAR).append('-').append(Constants.BLANK_CHAR).append(
				format(time, AM_PM_FORMATTER));
	return builder.toString();
	}

	public static String format(Date date, SimpleDateFormat formatter) {
	return date == null ? "" : formatter.format(date);
	}

	/**
	 * Makes a date String in the format MM/DD/YYYY from a Date
	 *
	 * @param date
	 *            The Date
	 * @return A date String in the format MM/DD/YYYY
	 */
	public static String toDateString(Date date) {
	return toDateString(date, "dd/MM/yyyy");
	}

	/**
	 * Makes a time String in the format HH:MM:SS from a Date. If the seconds
	 * are 0, then the output is in HH:MM.
	 *
	 * @param date
	 *            The Date
	 * @return A time String in the format HH:MM:SS or HH:MM
	 */
	public static String toTimeString(Date date) {
	if (date == null) return "";
	Calendar calendar = Calendar.getInstance();

	calendar.setTime(date);
	return (toTimeString(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar
			.get(Calendar.SECOND)));
	}

	/**
	 * Makes a time String in the format HH:MM:SS from a separate ints for hour,
	 * minute, and second. If the seconds are 0, then the output is in HH:MM.
	 *
	 * @param hour
	 *            The hour int
	 * @param minute
	 *            The minute int
	 * @param second
	 *            The second int
	 * @return A time String in the format HH:MM:SS or HH:MM
	 */
	public static String toTimeString(int hour, int minute, int second) {
	String hourStr;
	String minuteStr;
	String secondStr;

	if (hour < 10) {
		hourStr = "0" + hour;
	} else {
		hourStr = "" + hour;
	}
	if (minute < 10) {
		minuteStr = "0" + minute;
	} else {
		minuteStr = "" + minute;
	}
	if (second < 10) {
		secondStr = "0" + second;
	} else {
		secondStr = "" + second;
	}
	if (second == 0) {
		return hourStr + ":" + minuteStr;
	} else {
		return hourStr + ":" + minuteStr + ":" + secondStr;
	}
	}

	/**
	 * Makes a combined data and time string in the format "MM/DD/YYYY HH:MM:SS"
	 * from a Date. If the seconds are 0 they are left off.
	 *
	 * @param date
	 *            The Date
	 * @return A combined data and time string in the format
	 *         "MM/DD/YYYY HH:MM:SS" where the seconds are left off if they are
	 *         0.
	 */
	public static String toDateTimeString(Date date) {
	if (date == null) return "";
	String dateString = toDateString(date);
	String timeString = toTimeString(date);

	if (dateString != null && timeString != null) {
		return dateString + " " + timeString;
	} else {
		return "";
	}
	}

	public static String toGmtDateString(Date Date) {
	DateFormat df = DateFormat.getDateTimeInstance();
	df.setTimeZone(TimeZone.getTimeZone("GMT"));
	return df.format(Date);
	}

	/**
	 * Makes a Date for the beginning of the month
	 *
	 * @return A Date of the beginning of the month
	 */
	public static Date monthBegin() {
	Calendar mth = Calendar.getInstance();

	mth.set(Calendar.DAY_OF_MONTH, 1);
	mth.set(Calendar.HOUR_OF_DAY, 0);
	mth.set(Calendar.MINUTE, 0);
	mth.set(Calendar.SECOND, 0);
	mth.set(Calendar.MILLISECOND, 0);
	mth.set(Calendar.AM_PM, Calendar.AM);
	return new Date(mth.getTime().getTime());
	}

	/**
	 * returns a week number in a year for a Date input
	 *
	 * @param input
	 *            Date date
	 * @return A int containing the week number
	 */
	public static int weekNumber(Date input) {
	return weekNumber(input, TimeZone.getDefault(), Locale.getDefault());
	}

	/**
	 * returns a day number in a week for a Date input
	 *
	 * @param stamp
	 *            Date date
	 * @return A int containing the day number (sunday = 1, saturday = 7)
	 */
	public static int dayNumber(Date stamp) {
	Calendar tempCal = toCalendar(stamp, TimeZone.getDefault(), Locale.getDefault());
	return tempCal.get(Calendar.DAY_OF_WEEK);
	}

	public static int weekNumber(Date input, int startOfWeek) {
	Calendar calendar = Calendar.getInstance();
	calendar.setFirstDayOfWeek(startOfWeek);

	if (startOfWeek == Calendar.MONDAY) {
		calendar.setMinimalDaysInFirstWeek(4);
	} else
		if (startOfWeek == Calendar.SUNDAY) {
			calendar.setMinimalDaysInFirstWeek(3);
		}

	calendar.setTime(new Date(input.getTime()));
	return calendar.get(Calendar.WEEK_OF_YEAR);
	}

	// ----- New methods that take a timezone and locale -- //

	/**
	 * Returns a Calendar object initialized to the specified date/time, time
	 * zone, and locale.
	 *
	 * @param date
	 *            date/time to use
	 * @param timeZone
	 * @param locale
	 * @return Calendar object
	 * @see java.util.Calendar
	 */
	public static Calendar toCalendar(Date date, TimeZone timeZone, Locale locale) {
	Calendar cal = Calendar.getInstance(timeZone, locale);
	if (date != null) {
		cal.setTime(date);
	}
	return cal;
	}

	/**
	 * Perform date/time arithmetic on a Date. This is the only accurate way to
	 * perform date/time arithmetic across locales and time zones.
	 *
	 * @param stamp
	 *            date/time to perform arithmetic on
	 * @param adjType
	 *            the adjustment type to perform. Use one of the
	 *            java.util.Calendar fields.
	 * @param adjQuantity
	 *            the adjustment quantity.
	 * @param timeZone
	 * @param locale
	 * @return adjusted Date
	 * @see java.util.Calendar
	 */
	public static Date adjustDate(Date stamp, int adjType, int adjQuantity, TimeZone timeZone, Locale locale) {
	Calendar tempCal = toCalendar(stamp, timeZone, locale);
	tempCal.add(adjType, adjQuantity);
	return new Date(tempCal.getTimeInMillis());
	}

	public static Date adjustDate(Date stamp, Integer adjType, Integer adjQuantity) {
	Calendar tempCal = toCalendar(stamp);
	tempCal.add(adjType, adjQuantity);
	return new Date(tempCal.getTimeInMillis());
	}

	public static Date getDayStart(Date stamp, TimeZone timeZone, Locale locale) {
	return getDayStart(stamp, 0, timeZone, locale);
	}

	public static Date getDayStart(Date stamp, int daysLater, TimeZone timeZone, Locale locale) {
	Calendar tempCal = toCalendar(stamp, timeZone, locale);
	tempCal.set(tempCal.get(Calendar.YEAR), tempCal.get(Calendar.MONTH), tempCal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
	tempCal.add(Calendar.DAY_OF_MONTH, daysLater);
	Date retStamp = new Date(tempCal.getTimeInMillis());
	return retStamp;
	}

	public static Date getDayEnd(Date stamp, TimeZone timeZone, Locale locale) {
	return getDayEnd(stamp, Long.valueOf(0), timeZone, locale);
	}

	public static Date getDayEnd(Date stamp, Long daysLater, TimeZone timeZone, Locale locale) {
	Calendar tempCal = toCalendar(stamp, timeZone, locale);
	tempCal
			.set(tempCal.get(Calendar.YEAR), tempCal.get(Calendar.MONTH), tempCal.get(Calendar.DAY_OF_MONTH), 23, 59,
					59);
	tempCal.add(Calendar.DAY_OF_MONTH, daysLater.intValue());
	Date retStamp = new Date(tempCal.getTimeInMillis());
	return retStamp;
	}

	public static Date getWeekStart(Date stamp, TimeZone timeZone, Locale locale) {
	return getWeekStart(stamp, 0, 0, timeZone, locale);
	}

	public static Date getWeekStart(Date stamp, int daysLater, TimeZone timeZone, Locale locale) {
	return getWeekStart(stamp, daysLater, 0, timeZone, locale);
	}

	public static Date getWeekStart(Date stamp, int daysLater, int weeksLater, TimeZone timeZone, Locale locale) {
	Calendar tempCal = toCalendar(stamp, timeZone, locale);
	tempCal.set(tempCal.get(Calendar.YEAR), tempCal.get(Calendar.MONTH), tempCal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
	tempCal.add(Calendar.DAY_OF_MONTH, daysLater);
	tempCal.set(Calendar.DAY_OF_WEEK, tempCal.getFirstDayOfWeek());
	tempCal.add(Calendar.WEEK_OF_MONTH, weeksLater);
	Date retStamp = new Date(tempCal.getTimeInMillis());
	return retStamp;
	}

	public static Date getWeekEnd(Date stamp, TimeZone timeZone, Locale locale) {
	Calendar tempCal = toCalendar(stamp, timeZone, locale);
	tempCal.set(tempCal.get(Calendar.YEAR), tempCal.get(Calendar.MONTH), tempCal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
	tempCal.add(Calendar.WEEK_OF_MONTH, 1);
	tempCal.set(Calendar.DAY_OF_WEEK, tempCal.getFirstDayOfWeek());
	tempCal.add(Calendar.SECOND, -1);
	Date retStamp = new Date(tempCal.getTimeInMillis());
	return retStamp;
	}

	public static Date getMonthStart(Date stamp, TimeZone timeZone, Locale locale) {
	return getMonthStart(stamp, 0, 0, timeZone, locale);
	}

	public static Date getMonthStart(Date stamp, int daysLater, TimeZone timeZone, Locale locale) {
	return getMonthStart(stamp, daysLater, 0, timeZone, locale);
	}

	public static Date getMonthStart(Date stamp, int daysLater, int monthsLater, TimeZone timeZone, Locale locale) {
	Calendar tempCal = toCalendar(stamp, timeZone, locale);
	tempCal.set(tempCal.get(Calendar.YEAR), tempCal.get(Calendar.MONTH), 1, 0, 0, 0);
	tempCal.add(Calendar.MONTH, monthsLater);
	tempCal.add(Calendar.DAY_OF_MONTH, daysLater);
	Date retStamp = new Date(tempCal.getTimeInMillis());
	return retStamp;
	}

	public static Date getMonthEnd(Date stamp) {
	return getMonthEnd(stamp, TimeZone.getDefault(), Locale.getDefault());
	}

	public static Date getMonthEnd(Date stamp, TimeZone timeZone, Locale locale) {
	Calendar tempCal = toCalendar(stamp, timeZone, locale);
	tempCal.set(tempCal.get(Calendar.YEAR), tempCal.get(Calendar.MONTH), tempCal
			.getActualMaximum(Calendar.DAY_OF_MONTH), 0, 0, 0);
	return getDayEnd(new Date(tempCal.getTimeInMillis()), timeZone, locale);
	}

	public static Date getYearStart(Date stamp, TimeZone timeZone, Locale locale) {
	return getYearStart(stamp, 0, 0, 0, timeZone, locale);
	}

	public static Date getYearStart(Date stamp, int daysLater, TimeZone timeZone, Locale locale) {
	return getYearStart(stamp, daysLater, 0, 0, timeZone, locale);
	}

	public static Date getYearStart(Date stamp, int daysLater, int yearsLater, TimeZone timeZone, Locale locale) {
	return getYearStart(stamp, daysLater, 0, yearsLater, timeZone, locale);
	}

	public static Date getYearStart(Date stamp, Number daysLater, Number monthsLater, Number yearsLater,
			TimeZone timeZone, Locale locale) {
	return getYearStart(stamp, (daysLater == null ? 0 : daysLater.intValue()), (monthsLater == null ? 0 : monthsLater
			.intValue()), (yearsLater == null ? 0 : yearsLater.intValue()), timeZone, locale);
	}

	public static Date getYearStart(Date stamp, int daysLater, int monthsLater, int yearsLater, TimeZone timeZone,
			Locale locale) {
	Calendar tempCal = toCalendar(stamp, timeZone, locale);
	tempCal.set(tempCal.get(Calendar.YEAR), Calendar.JANUARY, 1, 0, 0, 0);
	tempCal.add(Calendar.YEAR, yearsLater);
	tempCal.add(Calendar.MONTH, monthsLater);
	tempCal.add(Calendar.DAY_OF_MONTH, daysLater);
	Date retStamp = new Date(tempCal.getTimeInMillis());
	return retStamp;
	}

	public static Date getYearEnd(Date stamp, TimeZone timeZone, Locale locale) {
	Calendar tempCal = toCalendar(stamp, timeZone, locale);
	tempCal.set(tempCal.get(Calendar.YEAR), tempCal.getActualMaximum(Calendar.MONTH) + 1, 0, 0, 0, 0);
	return getMonthEnd(new Date(tempCal.getTimeInMillis()), timeZone, locale);
	}

	public static int weekNumber(Date stamp, TimeZone timeZone, Locale locale) {
	Calendar tempCal = toCalendar(stamp, timeZone, locale);
	return tempCal.get(Calendar.WEEK_OF_YEAR);
	}

	/**
	 * Returns a List of day name Strings - suitable for calendar headings.
	 *
	 * @param locale
	 * @return List of day name Strings
	 */
	public static List<String> getDayNames(Locale locale) {
	Calendar tempCal = Calendar.getInstance(locale);
	tempCal.set(Calendar.DAY_OF_WEEK, tempCal.getFirstDayOfWeek());
	SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE", locale);
	List<String> resultList = new ArrayList<String>();
	for (int i = 0; i < 7; i++) {
		resultList.add(dateFormat.format(tempCal.getTime()));
		tempCal.roll(Calendar.DAY_OF_WEEK, 1);
	}
	return resultList;
	}

	/**
	 * Returns a List of month name Strings - suitable for calendar headings.
	 *
	 * @param locale
	 * @return List of month name Strings
	 */
	public static List<String> getMonthNames(Locale locale) {
	Calendar tempCal = Calendar.getInstance(locale);
	tempCal.set(Calendar.MONTH, Calendar.JANUARY);
	SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM", locale);
	List<String> resultList = new ArrayList<String>();
	for (int i = Calendar.JANUARY; i <= tempCal.getActualMaximum(Calendar.MONTH); i++) {
		resultList.add(dateFormat.format(tempCal.getTime()));
		tempCal.roll(Calendar.MONTH, 1);
	}
	return resultList;
	}

	/**
	 * Returns an initialized DateFormat object.
	 *
	 * @param dateFormat
	 *            optional format string
	 * @param tz
	 * @param locale
	 *            can be null if dateFormat is not null
	 * @return DateFormat object
	 */
	public static DateFormat toDateFormat(String dateFormat, TimeZone tz, Locale locale) {
	DateFormat df = null;
	if (dateFormat == null) {
		df = DateFormat.getDateInstance(DateFormat.SHORT, locale);
	} else {
		df = new SimpleDateFormat(dateFormat);
	}
	df.setTimeZone(tz);
	return df;
	}

	/**
	 * Returns an initialized DateFormat object.
	 *
	 * @param dateTimeFormat
	 *            optional format string
	 * @param tz
	 * @param locale
	 *            can be null if dateTimeFormat is not null
	 * @return DateFormat object
	 */
	public static DateFormat toDateTimeFormat(String dateTimeFormat, TimeZone tz, Locale locale) {
	DateFormat df = null;
	if (dateTimeFormat == null) {
		df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, locale);
	} else {
		df = new SimpleDateFormat(dateTimeFormat);
	}
	df.setTimeZone(tz);
	return df;
	}

	/**
	 * Returns an initialized DateFormat object.
	 *
	 * @param timeFormat
	 *            optional format string
	 * @param tz
	 * @param locale
	 *            can be null if timeFormat is not null
	 * @return DateFormat object
	 */
	public static DateFormat toTimeFormat(String timeFormat, TimeZone tz, Locale locale) {
	DateFormat df = null;
	if (timeFormat == null) {
		df = DateFormat.getTimeInstance(DateFormat.MEDIUM, locale);
	} else {
		df = new SimpleDateFormat(timeFormat);
	}
	df.setTimeZone(tz);
	return df;
	}

	/**
	 * Localized String to Date conversion. To be used in tandem with
	 * DateToString().
	 */
	public static Date stringToDate(String dateTimeString, TimeZone tz, Locale locale) throws ParseException {
	return stringToDate(dateTimeString, null, tz, locale);
	}

	/**
	 * Localized String to Date conversion. To be used in tandem with
	 * DateToString().
	 */
	public static Date stringToDate(String dateTimeString, String dateTimeFormat, TimeZone tz, Locale locale)
			throws ParseException {
	DateFormat dateFormat = toDateTimeFormat(dateTimeFormat, tz, locale);
	Date parsedDate = dateFormat.parse(dateTimeString);
	return new Date(parsedDate.getTime());
	}

	/**
	 * Localized Date to String conversion. To be used in tandem with
	 * stringToDate().
	 */
	public static String DateToString(Date stamp, TimeZone tz, Locale locale) {
	return DateToString(stamp, null, tz, locale);
	}

	/**
	 * Localized Date to String conversion. To be used in tandem with
	 * stringToDate().
	 */
	public static String DateToString(Date stamp, String dateTimeFormat, TimeZone tz, Locale locale) {
	DateFormat dateFormat = toDateTimeFormat(dateTimeFormat, tz, locale);
	dateFormat.setTimeZone(tz);
	return dateFormat.format(stamp);
	}

	/**
	 * Returns the OFBiz default TimeZone object. The default time zone is
	 * configured in the <code>start.properties</code> file (
	 * <code>ofbiz.timeZone.default</code>).
	 *
	 * @deprecated Okay to use TimeZone.getDefault()
	 * @see java.util.TimeZone
	 */
	@Deprecated
	public static TimeZone getDefaultTimeZone() {
	return TimeZone.getDefault();
	}

	public static int getSecond(Date stamp, TimeZone timeZone, Locale locale) {
	Calendar cal = UtilDateTime.toCalendar(stamp, timeZone, locale);
	return cal.get(Calendar.SECOND);
	}

	public static int getMinute(Date stamp, TimeZone timeZone, Locale locale) {
	Calendar cal = UtilDateTime.toCalendar(stamp, timeZone, locale);
	return cal.get(Calendar.MINUTE);
	}

	public static int getHour(Date stamp, TimeZone timeZone, Locale locale) {
	Calendar cal = UtilDateTime.toCalendar(stamp, timeZone, locale);
	return cal.get(Calendar.HOUR_OF_DAY);
	}

	public static int getDayOfWeek(Date stamp, TimeZone timeZone, Locale locale) {
	Calendar cal = UtilDateTime.toCalendar(stamp, timeZone, locale);
	return cal.get(Calendar.DAY_OF_WEEK);
	}

	public static int getDayOfMonth(Date stamp, TimeZone timeZone, Locale locale) {
	Calendar cal = UtilDateTime.toCalendar(stamp, timeZone, locale);
	return cal.get(Calendar.DAY_OF_MONTH);
	}

	public static int getDayOfYear(Date stamp, TimeZone timeZone, Locale locale) {
	Calendar cal = UtilDateTime.toCalendar(stamp, timeZone, locale);
	return cal.get(Calendar.DAY_OF_YEAR);
	}

	public static int getWeek(Date stamp, TimeZone timeZone, Locale locale) {
	Calendar cal = UtilDateTime.toCalendar(stamp, timeZone, locale);
	return cal.get(Calendar.WEEK_OF_YEAR);
	}

	public static int getMonth(Date stamp, TimeZone timeZone, Locale locale) {
	Calendar cal = UtilDateTime.toCalendar(stamp, timeZone, locale);
	return cal.get(Calendar.MONTH);
	}

	public static int getYear(Date stamp, TimeZone timeZone, Locale locale) {
	Calendar cal = UtilDateTime.toCalendar(stamp, timeZone, locale);
	return cal.get(Calendar.YEAR);
	}

	public static Date mergeDateTime(Date date, Date time) {
	Calendar dateC = Calendar.getInstance();
	dateC.setTime(date);
	Calendar dateT = Calendar.getInstance();
	dateT.setTime(time);
	dateT.set(Calendar.DATE, dateC.get(Calendar.DATE));
	dateT.set(Calendar.MONTH, dateC.get(Calendar.MONTH));
	dateT.set(Calendar.YEAR, dateC.get(Calendar.YEAR));
	return dateT.getTime();
	}

	/**
	 * Sets date fields(date month and year to 1970/1/1 as java's date starts
	 * from there)
	 */
	public static Date timeOnly(Date date) {
	Calendar dateC = Calendar.getInstance();
	dateC.setTime(date);
	dateC.set(Calendar.DATE, 1);
	dateC.set(Calendar.MONTH, 0);
	dateC.set(Calendar.YEAR, 1970);
	return dateC.getTime();
	}

	public static Date timeOnly() {
	return timeOnly(new Date());
	}

	public static Date nowDateOnly() {
	Calendar temp = Calendar.getInstance();
	Calendar calendar = Calendar.getInstance();
	calendar.clear();
	calendar.set(temp.get(Calendar.YEAR), temp.get(Calendar.MONTH), temp.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
	return calendar.getTime();
	}

	public static Date dateOnly(Date date) {
	Calendar calendar = Calendar.getInstance();
	calendar.setTime(date);
	calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0, 0,
			0);
	return calendar.getTime();
	}

	public static String convertTimeZone(Date date, TimeZone timeZone) {
	if (timeZone == null) timeZone = TimeZone.getDefault();
	SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
	dateFormat.setTimeZone(timeZone);
	return dateFormat.format(date);
	}

	public static String getWeekdayCaption(Date date) {
	Calendar c = Calendar.getInstance();
	c.setTime(date);
	return DAYS[c.get(Calendar.DAY_OF_WEEK) - 1];
	}

	public static String getWeekdayCaptionInShort(Date date) {
	Calendar c = Calendar.getInstance();
	c.setTime(date);
	return DAYSINSHORT[c.get(Calendar.DAY_OF_WEEK) - 1];
	}

	public static int getFromDate(Date date, int field) {
	Calendar calendar = Calendar.getInstance();
	calendar.setTime(date);
	return calendar.get(field);
	}

	public static Date[] findDateRangeForAge(int age) {
	Date lowerRange = addYearsToDate(nowDate(), -(age + 1));
	lowerRange = addDaysToDate(lowerRange, 1);
	return new Date[] { lowerRange, addYearsToDate(nowDate(), -age) };
	}

	public static String calculateAge(Date dateOfBirth, Date till) {
	if(till == null)
		till = new Date();
	long delta = UtilDateTime.getIntervalInHours(dateOfBirth, till);
	if (delta >= 0 && delta < 97)
		return delta + " Hour(s) ";
	else
		if (delta / 24 <= 28) {
			return (delta / 24) +" Day(s)";
		} else
			if (delta / (24 * 7) < 14)
				return (delta / (24 * 7)) + " Week(s)";
			else
				if (delta / (24 * 7 * 12) < 20)
					return getIntervalInMonths(dateOfBirth, new Date()) + " Month(s)";
				else
					return getIntervalInYearsAndMonths(dateOfBirth, till);
	}

	public static String calculateAge(Date dateOfBirth) {
	return calculateAge(dateOfBirth, new Date());
	}

	public static Date getDateOfBirth(int age) {
	Calendar c = Calendar.getInstance();
	c.add(Calendar.YEAR, -age);
	return c.getTime();
	}

	public static boolean checkAgeRangeInyears(Date dob, Long agefrom, Long ageto) {
	if (dob == null) return false;
	double ageInYears = UtilDateTime.getIntervalInMonths(dob, new Date()) / 12;
	if ((agefrom != null && ageInYears < agefrom)) return false;
	if ((ageto != null && ageInYears > ageto)) return false;
	return true;
	}

	public static Date convertDate(XMLGregorianCalendar xmlCalendar) {
	if (xmlCalendar == null) return null;
	Calendar calendar = Calendar.getInstance();
	calendar.set(Calendar.DATE, xmlCalendar.getDay());
	calendar.set(Calendar.MONTH, xmlCalendar.getMonth()-1);
	calendar.set(Calendar.YEAR, xmlCalendar.getYear());
	return calendar.getTime();
	}

	public static Date convertTime(XMLGregorianCalendar xmlCalendar) {
	if (xmlCalendar == null) return null;
	Calendar calendar = Calendar.getInstance();
	calendar.set(Calendar.HOUR, xmlCalendar.getHour());
	calendar.set(Calendar.MINUTE, xmlCalendar.getMinute());
	calendar.set(Calendar.SECOND, xmlCalendar.getSecond());
	return calendar.getTime();
	}

	public static Date parseDate(String dateString) {
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmSS");
	Date formattedDate = null;
	try {
		formattedDate = dateFormat.parse(dateString);
	} catch (ParseException e) {
		e.printStackTrace();
	}
	return formattedDate;
	}

	public static void main(String[] args) {
        System.out.println(getDateWhenGivenFollowupDays(1));
	}
	
	public static Date add(String field,Date date1,int value){
	if ("Days".equals(field)) return addDaysToDate(date1, value);
	if ("Weeks".equals(field)) return addDaysToDate(date1, value * 7);
	if ("Months".equals(field)) return addMonthsToDate(date1, value);
	return addYearsToDate(date1, value);
	}

	public static Date parseDateOfBirth(String dateString) {
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	Date formattedDate = null;
	try {
		formattedDate = dateFormat.parse(dateString);
	} catch (ParseException e) {
		e.printStackTrace();
	}
	return formattedDate;
	}
	
	public static Date createTime(Integer hr, Integer min, Integer sec){
	Date time = timeOnly();
	Calendar calendar = Calendar.getInstance();
	calendar.setTime(time);
	calendar.set(Calendar.AM_PM, hr > 12 ? Calendar.PM : Calendar.AM);
	calendar.set(Calendar.HOUR, hr > 12 ? hr - 12 : hr);
	calendar.set(Calendar.MINUTE, min);
	calendar.set(Calendar.SECOND, sec);
	return calendar.getTime();
	}
	
	public static Date getDateOnly(Date date) {
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	Date formattedDate = null;
	String dateString = dateFormat.format(date);
	try {
		formattedDate = dateFormat.parse(dateString);
	} catch (ParseException e) {
		e.printStackTrace();
	}
	return formattedDate;
	}
	
	public static boolean currentDateInBetween(Date from, Date thru) {
	return dateInBetween(from, thru, nowDate());
	}
	
	public static boolean dateInBetween(Date from, Date thru, Date candidateDate) {
	return candidateDate.getTime() >= from.getTime() && candidateDate.getTime() <= thru.getTime();
	}
	public static final int IGNORE_GRANULARITY = 10000; // In Milliseconds
	
	public static String formatDateToDatetimeFormat(Date date) {
		if(date == null)
			return " ";
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_TIMESTAMP_FORMAT);
		String dateString = dateFormat.format(date);
		return dateString;
	}
	
	public static String formatDateToddMMyyyyHHmmss(Date date) {
		if(date == null)
			return " ";
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
		String dateString = dateFormat.format(date);
		return dateString;
	}

    public static Date getDateWhenGivenFollowupDays(Integer followupDays){
        /*Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, followupDays);
        return calendar.getTime();*/
        /*if(followupDays != null && followupDays == 0) {
            LocalDate localDate = new LocalDate();
            localDate.plusDays(followupDays);
            return localDate.toDate();
        }*/
        if(followupDays != null) {
            LocalDate localDate = new LocalDate();
            localDate = localDate.plusDays(followupDays);
            return localDate.toDate();
        }
        return null;
    }

    public static Date getUpcomingDateByGivenDateAndNoOfDays(Date date, Integer followupDays){
        if(followupDays != null && date != null) {
            LocalDate localDate = new LocalDate(date);
            localDate = localDate.plusDays(followupDays);
            return localDate.toDate();
        }
        return null;
    }

    public static boolean isFirstDateAfterSecondDate(Date firstDate, Date secondDate){
        DateTime firstDateTime = new DateTime(firstDate);
        DateTime secondDateTime = new DateTime(secondDate);
        return firstDateTime.isAfter(secondDateTime.plusDays(1));


    }

    public static String constructTimeFromDateInAmPm(Date date){
        return AM_PM_FORMATTER.format(date);
    }

    public static Date constructFurnishedDate(Date appointmentDate, Date dateTime){
        LocalDateTime localDateTime = new LocalDateTime(appointmentDate);
        LocalDateTime localDate = new LocalDateTime(dateTime);
        LocalDateTime localDateTimeToBeFurnished = new LocalDateTime(localDateTime.getYear(), localDateTime.getMonthOfYear(), localDateTime.getDayOfMonth(), localDate.getHourOfDay(), localDate.getMinuteOfHour());
        return localDateTimeToBeFurnished.toDate();
    }

}
