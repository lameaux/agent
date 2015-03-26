package com.euromoby.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class NetUtils {

	public static String getHostname() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			return e.getMessage();
		}		
	}	
	
	public static String getIp() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			return e.getMessage();
		}		
	}
	
	public static List<String> getAllInterfaces() {
		List<String> list = new ArrayList<String>();
		try {
			Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
			while (e.hasMoreElements()) {
				NetworkInterface n = e.nextElement();
			    Enumeration<InetAddress> ee = n.getInetAddresses();
			    while (ee.hasMoreElements()) {
			    	InetAddress i = ee.nextElement();
			    	list.add(i.getHostAddress());
			    }
			}
		} catch (SocketException ignore) {
		}
		
		return list;
	}
	
}
