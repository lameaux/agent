package utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class IOUtils {
	public static void closeQuietly(Closeable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (IOException ioe) {
			// ignore
		}
	}

	public static String streamToString(InputStream is) {
		Scanner s = new Scanner(is, "UTF-8");
		try {
			return s.useDelimiter("\\A").next();
		} finally {
			s.close();
		}
	}

}
