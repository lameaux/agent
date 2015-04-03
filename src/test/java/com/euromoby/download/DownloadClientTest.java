package com.euromoby.download;

import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
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

@RunWith(MockitoJUnitRunner.class)
public class DownloadClientTest {

	private static final String URL = "https://localhost/file.zip";
	private static final boolean NO_PROXY = true;	
	
	@Mock
	Config config;
	@Mock
	CloseableHttpClient httpClient;
	@Mock
	CloseableHttpResponse closeableHttpResponse;
	
	@Mock
	HttpClientProvider httpClientProvider;
	
	DownloadClient downloadClient;

	@Before
	public void init() {
		downloadClient = new DownloadClient(config, httpClientProvider);
		Mockito.when(httpClientProvider.createHttpClient()).thenReturn(httpClient);
		Mockito.when(httpClientProvider.createRequestConfigBuilder(true)).thenReturn(RequestConfig.custom());
		Mockito.when(config.getAgentFilesPath()).thenReturn(System.getProperty("java.io.tmpdir"));
	}	
	
	@Test
	public void testDownload() throws Exception {
		
		Mockito.when(httpClient.execute(Matchers.any(HttpGet.class))).thenReturn(closeableHttpResponse);
		Mockito.when(closeableHttpResponse.getStatusLine()).thenReturn(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), HttpStatus.SC_OK, "OK"));
		
		byte[] responseContent = new byte[5000];
		Mockito.when(closeableHttpResponse.getEntity()).thenReturn(new ByteArrayEntity(responseContent));
		
		String fileName = String.valueOf(System.currentTimeMillis());
		downloadClient.download(URL, fileName, NO_PROXY);
	}

}
