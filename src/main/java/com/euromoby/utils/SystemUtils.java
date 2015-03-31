package com.euromoby.utils;

import java.io.File;

public class SystemUtils {

	public static String getUserHome() {
		return System.getProperty("user.home");
	}

	public static long getFreeSpace(String path) {
		return new File(path).getFreeSpace();
	}
	
}
