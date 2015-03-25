package utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {
	public static String iso(long timestamp) {
	    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    df.setTimeZone(TimeZone.getTimeZone("UTC"));
	    return df.format(new Date(timestamp));		
	}

	public static long fromIso(String timeString) throws ParseException {
	    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    df.setTimeZone(TimeZone.getTimeZone("UTC"));
	    return df.parse(timeString).getTime();		
	}	
	
}
