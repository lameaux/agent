package com.euromoby.download;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BasicHttpEntity;
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
	}	

	@Test
	public void testNotfound() throws Exception {
		Mockito.when(httpClient.execute(Matchers.any(HttpGet.class))).thenReturn(closeableHttpResponse);

		int status = HttpStatus.SC_NOT_FOUND;
		String reason = "Not found";		
		Mockito.when(closeableHttpResponse.getStatusLine()).thenReturn(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), HttpStatus.SC_NOT_FOUND, "Not found"));
		Mockito.when(closeableHttpResponse.getEntity()).thenReturn(new BasicHttpEntity());
		
		String fileName = String.valueOf(System.currentTimeMillis());
		try {
			downloadClient.download(URL, fileName, NO_PROXY);
			fail();
		} catch (Exception e) {
			assertEquals(status + " " + reason, e.getMessage());
		}
	}	
	
	@Test
	public void testDownload() throws Exception {
		Mockito.when(httpClient.execute(Matchers.any(HttpGet.class))).thenReturn(closeableHttpResponse);
		int status = HttpStatus.SC_OK;
		String reason = "OK";
		Mockito.when(closeableHttpResponse.getStatusLine()).thenReturn(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), status, reason));
		
		byte[] responseContent = new byte[5000];
		Arrays.fill(responseContent, (byte)42);
		Mockito.when(closeableHttpResponse.getEntity()).thenReturn(new ByteArrayEntity(responseContent));
		
		File tmpFile = File.createTempFile("prefix", "suffix", new File(System.getProperty("java.io.tmpdir")));
		tmpFile.deleteOnExit();
		Mockito.when(config.getAgentFilesPath()).thenReturn(System.getProperty("java.io.tmpdir"));		
		downloadClient.download(URL, tmpFile.getName(), NO_PROXY);
		assertEquals(responseContent.length, tmpFile.length());
		assertArrayEquals(responseContent, FileUtils.readFileToByteArray(tmpFile));
	}

}
