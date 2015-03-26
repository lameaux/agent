package com.euromoby.ping;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.Config;
import com.euromoby.model.PingInfo;
import com.euromoby.rest.handler.ping.PingHandler;
import com.euromoby.utils.HttpUtils;
import com.google.gson.Gson;

@Component
public class PingSender {

	private static final String PING_INFO_INPUT_NAME = "pingInfo";

	private static final Gson gson = new Gson();

	private Config config;

	@Autowired
	public PingSender(Config config) {
		this.config = config;
	}

	public PingInfo ping(String host, int restPort, boolean noProxy) throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();

		PingInfo myPingInfo = new PingInfo(config.getAgentId());

		try {
			RequestConfig.Builder requestConfigBuilder = HttpUtils.createRequestConfigBuilder(config, noProxy);

			String url = "http://" + host + ":" + restPort + PingHandler.URL;
			HttpUriRequest ping = RequestBuilder.post(url).setConfig(requestConfigBuilder.build()).addParameter(PING_INFO_INPUT_NAME, gson.toJson(myPingInfo))
					.build();

			CloseableHttpResponse response = httpclient.execute(ping);
			try {
				HttpEntity entity = response.getEntity();
				String content = EntityUtils.toString(entity);
				EntityUtils.consumeQuietly(entity);
				PingInfo receivedPingInfo = gson.fromJson(content, PingInfo.class);
				receivedPingInfo.getAgentId().setHost(ping.getURI().getHost());
				return receivedPingInfo;
			} finally {
				response.close();
			}

		} finally {
			httpclient.close();
		}

	}

}
