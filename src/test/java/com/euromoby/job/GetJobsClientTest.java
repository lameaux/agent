package com.euromoby.job;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.euromoby.agent.Config;
import com.euromoby.http.HttpClientProvider;
import com.euromoby.job.model.JobDetail;
import com.euromoby.model.AgentId;
import com.euromoby.rest.RestServer;
import com.euromoby.rest.handler.job.GetJobsHandler;
import com.google.gson.Gson;

@RunWith(MockitoJUnitRunner.class)
public class GetJobsClientTest {

	@Mock
	Config config;
	@Mock
	HttpClientProvider httpClientProvider;
	@Mock
	RequestConfig.Builder requestConfigBuilder;
	@Mock
	CloseableHttpResponse response;
	
	Gson gson = new Gson();
	
	AgentId myAgentId = new AgentId("my:21000");
	AgentId targetAgentId = new AgentId("target:21000");
	boolean NO_PROXY = false;
	
	GetJobsClient getJobsClient;

	@Before
	public void init() throws Exception {
		Mockito.when(config.getAgentId()).thenReturn(myAgentId);
		Mockito.when(httpClientProvider.createRequestConfigBuilder(Matchers.eq(targetAgentId.getHost()), Matchers.eq(NO_PROXY))).thenReturn(requestConfigBuilder);
		Mockito.when(requestConfigBuilder.build()).thenReturn(null);
		Mockito.when(httpClientProvider.executeRequest(Matchers.any(HttpUriRequest.class))).thenReturn(response);		
		getJobsClient = new GetJobsClient(config, httpClientProvider);
	}
	
	@Test
	public void testCreateRequest() throws Exception {
		HttpUriRequest request = getJobsClient.createRequest(targetAgentId, myAgentId, requestConfigBuilder);
		URI requestUri = request.getURI();
		
		assertEquals("https", requestUri.getScheme());
		assertEquals(targetAgentId.getHost(), requestUri.getHost());
		assertEquals(targetAgentId.getBasePort() + RestServer.REST_PORT, requestUri.getPort());
		assertEquals(GetJobsHandler.URL, requestUri.getPath());
		assertEquals(GetJobsHandler.AGENT_ID_PARAM_NAME + "=" + myAgentId.toString(), requestUri.getQuery());
	}
	
	@Test
	public void testGetJobsBadResponseCode() throws Exception {
		
		int status = HttpStatus.SC_BAD_REQUEST;
		String reasonPhrase = "Error";
		
		Mockito.when(response.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_BAD_REQUEST, reasonPhrase));
		
		try {
			getJobsClient.getJobs(targetAgentId, NO_PROXY);
			fail();
		} catch (Exception e) {
			assertEquals(status + " " + reasonPhrase, e.getMessage());
		}
		
		Mockito.verify(response).close();
	}

	@Test
	public void testGetJobsSuccess() throws Exception {
		
		Mockito.when(response.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK"));
		
		JobDetail[] jobDetails = new JobDetail[]{ new JobDetail(), new JobDetail() };
		
		HttpEntity entity = new ByteArrayEntity(gson.toJson(jobDetails).getBytes(), ContentType.APPLICATION_JSON);
		Mockito.when(response.getEntity()).thenReturn(entity);
		
		JobDetail[] receivedJobDetails = getJobsClient.getJobs(targetAgentId, NO_PROXY);
		assertArrayEquals(jobDetails, receivedJobDetails);

		Mockito.verify(response).close();
	}

	
}
