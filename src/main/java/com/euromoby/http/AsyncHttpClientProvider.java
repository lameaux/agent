package com.euromoby.http;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.Config;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.ProxyServer;
import com.ning.http.client.Realm.AuthScheme;
import com.ning.http.client.RequestBuilder;

@Component
public class AsyncHttpClientProvider {

	public static final Pattern WILDCARD_REGEX = Pattern.compile("[^*]+|(\\*)");	
	
	private Config config;
	private SSLContextProvider sslContextProvider;
	
	@Autowired
	public AsyncHttpClientProvider(Config config, SSLContextProvider sslContextProvider) {
		this.config = config;
		this.sslContextProvider = sslContextProvider;
	}
	
	public AsyncHttpClient createAsyncHttpClient() {
		
		AsyncHttpClientConfig.Builder configBuilder  = new AsyncHttpClientConfig.Builder();
		//int timeout = config.getHttpClientTimeout();
		//configBuilder.setConnectTimeout(timeout);
		//configBuilder.setReadTimeout(timeout);
		//configBuilder.setRequestTimeout(-1);
		configBuilder.setAllowPoolingConnections(true);
		//configBuilder.setAllowPoolingSslConnections(true);		
		configBuilder.setFollowRedirect(true);
		//configBuilder.setUserAgent(config.getHttpUserAgent());
		//configBuilder.setSSLContext(sslContextProvider.getSSLContext());
		//configBuilder.setAcceptAnyCertificate(true);
		
		return new AsyncHttpClient(configBuilder.build());

	}

	public RequestBuilder createRequestBuilder(String host, boolean noProxy) {
		RequestBuilder requestBuilder = new RequestBuilder();
		
		if (!noProxy && config.isHttpProxy() && !bypassProxy(host)) {
			ProxyServer proxyServer = new ProxyServer(config.getHttpProxyHost(), config.getHttpProxyPort());
			if (config.isHttpProxyAuthentication()) {
				proxyServer = new ProxyServer(config.getHttpProxyHost(), config.getHttpProxyPort(), config.getHttpProxyLogin(), config.getHttpProxyPassword());
				proxyServer.setScheme(AuthScheme.BASIC);
			}
			requestBuilder.setProxyServer(proxyServer);
		}
		return requestBuilder;
	}

	protected boolean bypassProxy(String host) {
		String[] proxyBypass = config.getHttpProxyBypass();
		
		for (String mask : proxyBypass) {
			Matcher m = WILDCARD_REGEX.matcher(mask);
			StringBuffer b = new StringBuffer();
			while (m.find()) {
				if (m.group(1) != null) {
					m.appendReplacement(b, ".*");
				} else {
					m.appendReplacement(b, "\\\\Q" + m.group(0) + "\\\\E");
				}
			}
			m.appendTail(b);
			
			if (host.matches(b.toString())) {
				return true;
			}
		}
		
		return false;
	}
	
	public static void main(String args[]) {
		AsyncHttpClient client = new AsyncHttpClient();
		client.prepareGet("http://www.hovnokod.cz").execute();
	}
}
