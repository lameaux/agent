package com.euromoby.agent;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class SSLContextProvider {

	private Config config;
	private SSLContext sslContext;

	@Autowired
	public SSLContextProvider(Config config) throws Exception {
		this.config = config;
		initSSLContext();
	}

	private void initSSLContext() throws Exception {
		File keystoreFile = getKeystoreFile();
		InputStream keystoreInputStream = new FileInputStream(keystoreFile);
		try {
			KeyStore keystore = KeyStore.getInstance("JKS");
			keystore.load(keystoreInputStream, config.getKeystoreStorePass().toCharArray());
			
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(keystore, config.getKeystoreKeyPass().toCharArray());
			
			TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("PKIX");
			trustManagerFactory.init(keystore);
			
			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
			
		} finally {
			IOUtils.closeQuietly(keystoreInputStream);
		}
	}
	
	private File getKeystoreFile() throws Exception {
		String keystorePath = config.getKeystorePath();
		if (keystorePath == null) {
			ClassPathResource cpr = new ClassPathResource("agent.keystore", Agent.class);
			return cpr.getFile();
		}
		return new File(keystorePath);
	}
	
	public SSLEngine newSslEngine() {
		SSLEngine sslEngine = sslContext.createSSLEngine();
		sslEngine.setUseClientMode(false);
		return sslEngine;
	}

	public SSLContext getSSLContext() {
		return sslContext;
	}
	
}
