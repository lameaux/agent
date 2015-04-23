package com.euromoby.http;

import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.auth.BasicSchemeFactory;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.AgentManager;
import com.euromoby.agent.Config;
import com.euromoby.model.AgentId;
import com.euromoby.rest.RestServer;

@Component
public class HttpClientProvider {

	public static final String HTTPS = "https";
	
	private Config config;
	private AgentManager agentManager;
	private SSLContextProvider sslContextProvider;

	@Autowired
	public HttpClientProvider(Config config, AgentManager agentManager, SSLContextProvider sslContextProvider) {
		this.config = config;
		this.agentManager = agentManager;
		this.sslContextProvider = sslContextProvider;
	}

	public CloseableHttpClient createHttpClient() {
		return HttpClientBuilder.create().setSslcontext(sslContextProvider.getSSLContext()).setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
	}

	public HttpClientContext createHttpClientContext() {
		HttpClientContext context = HttpClientContext.create();
		
		Registry<AuthSchemeProvider> authSchemeRegistry = RegistryBuilder.<AuthSchemeProvider>create()
	            .register("basic", new BasicSchemeFactory()).build();
	    context.setAuthSchemeRegistry(authSchemeRegistry);

	    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        context.setCredentialsProvider(credentialsProvider);	

	    AuthCache authCache = new BasicAuthCache();
        context.setAuthCache(authCache);

	    BasicScheme basicAuth = new BasicScheme();
	    UsernamePasswordCredentials userPasswordCredential = new UsernamePasswordCredentials(config.getRestLogin(), config.getRestPassword());
	    
	    List<AgentId> agents = agentManager.getAll();
	    for (AgentId agentId : agents) {
	    	int restPort = agentId.getBasePort() + RestServer.REST_PORT; 
		    credentialsProvider.setCredentials(new AuthScope(agentId.getHost(), restPort), userPasswordCredential);	    	
		    authCache.put(new HttpHost(agentId.getHost(), restPort, HTTPS), basicAuth);
	    }
        
        return context;
	}
	
	public RequestConfig.Builder createRequestConfigBuilder(boolean noProxy) {
		RequestConfig.Builder requestConfigBuilder = RequestConfig.custom().setSocketTimeout(3000).setConnectTimeout(3000);
		if (!noProxy && config.isHttpProxy()) {
			requestConfigBuilder.setProxy(new HttpHost(config.getHttpProxyHost(), config.getHttpProxyPort()));
		}
		return requestConfigBuilder;
	}

	
}
