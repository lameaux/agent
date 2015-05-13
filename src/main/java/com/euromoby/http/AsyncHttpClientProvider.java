package com.euromoby.http;

import java.net.URI;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.Config;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.ProxyServer;
import com.ning.http.client.Realm.AuthScheme;

@Component
public class AsyncHttpClientProvider implements DisposableBean {

	public static final Pattern WILDCARD_REGEX = Pattern.compile("[^*]+|(\\*)");	
	
	private Config config;
	private SSLContextProvider sslContextProvider;
	private AsyncHttpClient asyncHttpClient;
	
	@Autowired
	public AsyncHttpClientProvider(Config config, SSLContextProvider sslContextProvider) {
		this.config = config;
		this.sslContextProvider = sslContextProvider;
		asyncHttpClient = createAsyncHttpClient();
	}
	
	protected AsyncHttpClient createAsyncHttpClient() {
		AsyncHttpClientConfig.Builder configBuilder  = new AsyncHttpClientConfig.Builder();
		int timeout = config.getHttpClientTimeout();
		configBuilder.setConnectTimeout(timeout);
		configBuilder.setReadTimeout(timeout);
		configBuilder.setRequestTimeout(-1);
		configBuilder.setAllowPoolingConnections(true);
		configBuilder.setAllowPoolingSslConnections(true);		
		configBuilder.setFollowRedirect(true);
		configBuilder.setUserAgent(config.getHttpUserAgent());
		configBuilder.setSSLContext(sslContextProvider.getSSLContext());
		configBuilder.setAcceptAnyCertificate(true);
		return new AsyncHttpClient(configBuilder.build());
	}

	public BoundRequestBuilder prepareGet(String url, boolean noProxy) throws Exception {
		BoundRequestBuilder boundRequestBuilder = asyncHttpClient.prepareGet(url);
		
		URI uri = new URI(url);	
		if (!noProxy && config.isHttpProxy() && !HttpUtils.bypassProxy(config.getHttpProxyBypass(), uri.getHost())) {
			ProxyServer proxyServer = new ProxyServer(config.getHttpProxyHost(), config.getHttpProxyPort());
			if (config.isHttpProxyAuthentication()) {
				proxyServer = new ProxyServer(config.getHttpProxyHost(), config.getHttpProxyPort(), config.getHttpProxyLogin(), config.getHttpProxyPassword());
				proxyServer.setScheme(AuthScheme.BASIC);
			}
			boundRequestBuilder.setProxyServer(proxyServer);
		}
		
		return boundRequestBuilder;
		
	}

	@Override
	public void destroy() throws Exception {
		IOUtils.closeQuietly(asyncHttpClient);
		
	}

}
