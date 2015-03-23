package utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetUtils {

	public static String getHostname() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			return e.getMessage();
		}		
	}
	
}
