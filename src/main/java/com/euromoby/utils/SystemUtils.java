package com.euromoby.utils;

import java.io.File;

public class SystemUtils {

	public static String getUserHome() {
		return System.getProperty("user.home");
	}

	public static long getFreeSpace(String path) {
		File file = new File(path);
		while (!file.exists()) {
			if (file.getParentFile() == null) {
				break;
			}
			file = file.getParentFile();
		}
		return file.getFreeSpace();
	}

}
