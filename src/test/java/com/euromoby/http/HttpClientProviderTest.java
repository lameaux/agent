package com.euromoby.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.security.Principal;
import java.util.Arrays;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.euromoby.agent.AgentManager;
import com.euromoby.agent.Config;
import com.euromoby.model.AgentId;
import com.euromoby.rest.RestServer;

@RunWith(MockitoJUnitRunner.class)
public class HttpClientProviderTest {

	@Mock
	Config config;
	@Mock
	AgentManager agentManager;
	@Mock
	SSLContextProvider sslContextProvider;

	
	HttpClientProvider httpClientProvider;

	@Before
	public void init() {
		httpClientProvider = new HttpClientProvider(config, agentManager, sslContextProvider);
	}

	@Test
	public void testCreateHttpClient() throws Exception {
		SSLContext sslContext = SSLContext.getDefault();
		Mockito.when(sslContextProvider.getSSLContext()).thenReturn(sslContext);
		CloseableHttpClient closeableHttpClient = httpClientProvider.createHttpClient();
		assertNotNull(closeableHttpClient);
		
		Mockito.verify(sslContextProvider).getSSLContext();
	}


	@Test
	public void testCreateHttpClientContext() {
		
		String AUTH_BASIC = "basic";
		String LOGIN = "login";
		String PASS = "pass";
		AgentId agentId = new AgentId("agent:21000");
		int restPort = agentId.getBasePort() + RestServer.REST_PORT; 
		
		Mockito.when(config.getRestLogin()).thenReturn(LOGIN);
		Mockito.when(config.getRestPassword()).thenReturn(PASS);
		Mockito.when(agentManager.getAll()).thenReturn(Arrays.asList(agentId));
		
		HttpClientContext context = httpClientProvider.createHttpClientContext();
		
		Registry<AuthSchemeProvider> registry = (Registry<AuthSchemeProvider>) context.getAuthSchemeRegistry();
		assertNotNull(registry.lookup(AUTH_BASIC));
		
		CredentialsProvider credentialsProvider = context.getCredentialsProvider();
		AuthScope authScope = new AuthScope(agentId.getHost(), restPort);
		Credentials credentials = credentialsProvider.getCredentials(authScope);
		Principal principal = credentials.getUserPrincipal();
		assertEquals(LOGIN, principal.getName());
		assertEquals(PASS, credentials.getPassword());		
		
		AuthCache authCache = context.getAuthCache();
		HttpHost httpHost = new HttpHost(agentId.getHost(), restPort, HttpClientProvider.HTTPS);
		assertNotNull(authCache.get(httpHost));
		assertTrue(authCache.get(httpHost) instanceof BasicScheme);
	}
	
	@Test
	public void testNoProxyConfiguration() {

		Mockito.when(config.isHttpProxy()).thenReturn(false);
		
		boolean noProxy = true;
		RequestConfig rcTrue = httpClientProvider.createRequestConfigBuilder(null, noProxy).build();
		assertNull(rcTrue.getProxy());
		
		noProxy = false;
		RequestConfig rcFalse = httpClientProvider.createRequestConfigBuilder(null, noProxy).build();
		assertNull(rcFalse.getProxy());

		
	}

	@Test
	public void testProxyAvailable() {

		String PROXY_HOST = "proxy";
		int PROXY_PORT = 3128;
		
		Mockito.when(config.isHttpProxy()).thenReturn(true);
		Mockito.when(config.getHttpProxyHost()).thenReturn(PROXY_HOST);
		Mockito.when(config.getHttpProxyPort()).thenReturn(PROXY_PORT);
		Mockito.when(config.getHttpProxyBypass()).thenReturn(new String[]{});
		
		boolean noProxy = true;
		RequestConfig rcTrue = httpClientProvider.createRequestConfigBuilder(null, noProxy).build();
		assertNull(rcTrue.getProxy());
		
		noProxy = false;
		RequestConfig rcFalse = httpClientProvider.createRequestConfigBuilder(null, noProxy).build();
		HttpHost httpHost = rcFalse.getProxy();
		assertEquals(PROXY_HOST, httpHost.getHostName());
		assertEquals(PROXY_PORT, httpHost.getPort());

		
	}
	
	
}
