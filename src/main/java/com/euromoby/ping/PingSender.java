package com.euromoby.ping;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.http.HttpClientProvider;
import com.euromoby.ping.model.PingInfo;
import com.euromoby.rest.handler.ping.PingHandler;
import com.google.gson.Gson;

@Component
public class PingSender {

	private static final String URL_PATTERN = "https://%s:%d";	

	private static final Gson gson = new Gson();

	private HttpClientProvider httpClientProvider;
	private PingInfoProvider pingInfoProvider;

	@Autowired
	public PingSender(HttpClientProvider httpClientProvider, PingInfoProvider pingInfoProvider) {
		this.httpClientProvider = httpClientProvider;
		this.pingInfoProvider = pingInfoProvider;
	}

	public PingInfo ping(String host, int restPort, boolean noProxy) throws Exception {

		PingInfo myPingInfo = pingInfoProvider.createPingInfo();

			RequestConfig.Builder requestConfigBuilder = httpClientProvider.createRequestConfigBuilder(host, noProxy);

			String url = String.format(URL_PATTERN, host, restPort) + PingHandler.URL;
			HttpUriRequest request = RequestBuilder.post(url)
					.setConfig(requestConfigBuilder.build())
					.addParameter(PingHandler.PING_INFO_INPUT_NAME, gson.toJson(myPingInfo))
					.build();

			CloseableHttpResponse response = httpClientProvider.executeRequest(request);
			try {
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
					EntityUtils.consumeQuietly(response.getEntity());
					throw new Exception(statusLine.getStatusCode() + " " + statusLine.getReasonPhrase());
				}				
				
				HttpEntity entity = response.getEntity();
				String content = EntityUtils.toString(entity);
				EntityUtils.consumeQuietly(entity);
				PingInfo receivedPingInfo = gson.fromJson(content, PingInfo.class);
				receivedPingInfo.getAgentId().setHost(request.getURI().getHost());
				return receivedPingInfo;
			} finally {
				response.close();
			}

	}

}
