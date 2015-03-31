package com.euromoby.rest;

import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

public class SslEngineFactory {

	private String storePassword;
	private String keyPassword;

	
	public SslEngineFactory() {
		storePassword = "123456";
		keyPassword = "123456";
	}

	public SSLEngine newSslEngine() throws Exception {
		
		KeyStore ks = KeyStore.getInstance("JKS");
		InputStream ksInputStream = RestServer.class.getClassLoader().getResourceAsStream("agent.keystore");
		ks.load(ksInputStream, storePassword.toCharArray());

		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		kmf.init(ks, keyPassword.toCharArray());

		// TODO share ssl context
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(kmf.getKeyManagers(), null, null);
		
		SSLEngine sslEngine = sslContext.createSSLEngine();
		sslEngine.setUseClientMode(false);
		return sslEngine;
	}

}
