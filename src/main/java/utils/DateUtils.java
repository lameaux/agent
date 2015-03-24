package utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {
	public static String iso(long timestamp) {
	    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
	    df.setTimeZone(TimeZone.getTimeZone("UTC"));
	    return df.format(new Date(timestamp));		
	}
	
}
