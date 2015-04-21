package com.euromoby.cdn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.http.HttpVersion;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.euromoby.agent.AgentManager;
import com.euromoby.agent.Config;
import com.euromoby.http.HttpClientProvider;
import com.euromoby.model.AgentId;
import com.euromoby.rest.handler.fileinfo.FileInfo;

@RunWith(MockitoJUnitRunner.class)
public class CdnNetworkTest {

	@Mock
	Config config;
	@Mock
	AgentManager agentManager;
	@Mock
	HttpClientProvider httpClientProvider;
	@Mock
	CdnResourceMapping cdnResourceMapping;
	@Mock
	CdnResource cdnResource;
	@Mock
	CloseableHttpClient httpClient;
	@Mock 
	RequestConfig.Builder requestConfigBuilder;
	@Mock
	CloseableHttpResponse response;

	CdnNetwork cdnNetwork;

	private static final String GOOD_URL = "/good";
	private static final String BAD_URL = "/bad";	
	
	@Before
	public void init() {
		Mockito.when(config.getCdnPoolSize()).thenReturn(1);
		Mockito.when(cdnResourceMapping.findByUrl(Matchers.eq(GOOD_URL))).thenReturn(cdnResource);
		Mockito.when(cdnResourceMapping.findByUrl(Matchers.eq(BAD_URL))).thenReturn(null);		
		cdnNetwork = new CdnNetwork(config, agentManager, httpClientProvider, cdnResourceMapping);
	}
	
	@Test
	public void testUrlIsAvailable() {
		assertTrue(cdnNetwork.isAvailable(GOOD_URL));
	}

	@Test
	public void testUrlIsNotAvailable() {
		assertFalse(cdnNetwork.isAvailable(BAD_URL));
	}
	
	@Test
	public void testSendRequestsToActiveAgents() throws Exception {
		Mockito.when(agentManager.getActive()).thenReturn(Collections.singletonList(new AgentId("agent1:21000")));
		Mockito.when(httpClientProvider.createHttpClient()).thenReturn(httpClient);
		Mockito.when(httpClientProvider.createRequestConfigBuilder(Matchers.eq(false))).thenReturn(requestConfigBuilder);
		Mockito.when(httpClient.execute(Matchers.any(HttpUriRequest.class), Matchers.any(HttpClientContext.class))).thenReturn(response);
		Mockito.when(response.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, 404, "Not found"));
		List<Future<FileInfo>> futures = cdnNetwork.sendRequestsToActiveAgents(GOOD_URL);
		assertEquals(1, futures.size());
		Future<FileInfo> futureFileInfo = futures.get(0);
		// 404 not found = null
		assertNull(futureFileInfo.get());
	}
	
	
}
