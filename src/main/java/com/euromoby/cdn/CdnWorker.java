package com.euromoby.cdn;

import java.util.concurrent.Callable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import com.euromoby.http.HttpClientProvider;
import com.euromoby.model.AgentId;
import com.euromoby.rest.RestServer;
import com.euromoby.rest.handler.fileinfo.FileInfo;
import com.euromoby.rest.handler.fileinfo.FileInfoHandler;
import com.google.gson.Gson;


public class CdnWorker implements Callable<FileInfo> {

	private static final String URL_PATTERN = "https://%s:%d";	
	private static final Gson gson = new Gson();	
	
	private AgentId agentId;
	private HttpClientProvider httpClientProvider;
	private String uriPath;
	

	public CdnWorker(HttpClientProvider httpClientProvider, AgentId agentId, String uriPath) {
		this.agentId = agentId;
		this.httpClientProvider = httpClientProvider;
		this.uriPath = uriPath;
	}

	@Override
	public FileInfo call() throws Exception {
		CloseableHttpClient httpclient = httpClientProvider.createHttpClient();

		try {

			RequestConfig.Builder requestConfigBuilder = httpClientProvider.createRequestConfigBuilder(false);
			String url = String.format(URL_PATTERN, agentId.getHost(), (agentId.getBasePort() + RestServer.REST_PORT)) + FileInfoHandler.URL + uriPath;
			HttpUriRequest request = RequestBuilder.get(url).setConfig(requestConfigBuilder.build())
					.build();

			CloseableHttpResponse response = httpclient.execute(request, httpClientProvider.createHttpClientContext());
			try {
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
					EntityUtils.consumeQuietly(response.getEntity());
					return null;
				}
				HttpEntity entity = response.getEntity();
				String content = EntityUtils.toString(entity);
				EntityUtils.consumeQuietly(entity);
				FileInfo fileInfo = gson.fromJson(content, FileInfo.class);
				fileInfo.setAgentId(agentId);
				return fileInfo;
			} finally {
				response.close();
			}
		} finally {
			httpclient.close();
		}
	}	
	
}
