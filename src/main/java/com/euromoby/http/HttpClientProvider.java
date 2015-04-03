package com.euromoby.http;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.Config;

@Component
public class HttpClientProvider {

	private Config config;
	private SSLContextProvider sslContextProvider;

	@Autowired
	public HttpClientProvider(Config config, SSLContextProvider sslContextProvider) {
		this.config = config;
		this.sslContextProvider = sslContextProvider;
	}

	public CloseableHttpClient createHttpClient() {
		return HttpClientBuilder.create().setSslcontext(sslContextProvider.getSSLContext()).setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
	}

	public RequestConfig.Builder createRequestConfigBuilder(boolean noProxy) {
		RequestConfig.Builder requestConfigBuilder = RequestConfig.custom().setSocketTimeout(3000).setConnectTimeout(3000);
		if (!noProxy && config.isHttpProxy()) {
			requestConfigBuilder.setProxy(new HttpHost(config.getHttpProxyHost(), config.getHttpProxyPort()));
		}
		return requestConfigBuilder;
	}

}
