package com.euromoby.utils;

import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

public class SSLUtils {

	public static SSLContext createSSLContext(InputStream ksInputStream, String storePassword, String keyPassword) throws Exception {

		KeyStore ks = KeyStore.getInstance("JKS");
		ks.load(ksInputStream, storePassword.toCharArray());
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(ks, keyPassword.toCharArray());
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(kmf.getKeyManagers(), null, null);
		return sslContext;
	}

}
