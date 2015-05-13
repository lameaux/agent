package com.euromoby.download.client;

import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.download.rest.DownloadRequestHandler;
import com.euromoby.http.HttpClientProvider;
import com.euromoby.model.AgentId;
import com.euromoby.ping.PingInfoProvider;
import com.euromoby.rest.RestServer;

@Component
public class DownloadRequestSender {

	private static final String URL_PATTERN = "https://%s:%d";	

	private HttpClientProvider httpClientProvider;

	@Autowired
	public DownloadRequestSender(HttpClientProvider httpClientProvider, PingInfoProvider pingInfoProvider) {
		this.httpClientProvider = httpClientProvider;
	}

	public void sendDownloadRequest(AgentId agentId, String url, String fileLocation) throws Exception {
			RequestConfig.Builder requestConfigBuilder = httpClientProvider.createRequestConfigBuilder(agentId.getHost(), false);

			String agentUrl = String.format(URL_PATTERN, agentId.getHost(), agentId.getBasePort() + RestServer.REST_PORT) + DownloadRequestHandler.URL;
			HttpUriRequest request = RequestBuilder.post(agentUrl)
					.setConfig(requestConfigBuilder.build())
					.addParameter(DownloadRequestHandler.REQUEST_INPUT_URL, url)
					.addParameter(DownloadRequestHandler.REQUEST_INPUT_FILE_LOCATION, fileLocation)
					.build();

			CloseableHttpResponse response = httpClientProvider.executeRequest(request);
			try {
				StatusLine statusLine = response.getStatusLine();
				EntityUtils.consumeQuietly(response.getEntity());
				if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
					throw new Exception(statusLine.getStatusCode() + " " + statusLine.getReasonPhrase());
				}				
			} finally {
				response.close();
			}


	}

}
