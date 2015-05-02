package com.euromoby.utils;

import java.util.Properties;

public class StringUtils {

	public static final String CRLF = "\r\n";	
	
	public static boolean nullOrEmpty(String value) {
		return value == null || value.trim().isEmpty();
	}	
	
	public static String trimIfNotEmpty(String value) {
		if (nullOrEmpty(value)) {
			return value;
		}
		return value.trim();
	}
	
	public static String printProperties(Properties p, String prefix) {
		StringBuffer sb = new StringBuffer();
		for(String propertyName : p.stringPropertyNames()) {
			if (prefix != null && !propertyName.startsWith(prefix)) {
				continue;
			}
			String value = p.getProperty(propertyName);
			sb.append(propertyName).append("=").append(value).append(CRLF);
		}
		return sb.toString();
	}
}
