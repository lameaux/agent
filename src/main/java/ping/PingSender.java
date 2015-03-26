package ping;

import model.PingInfo;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import agent.Agent;
import agent.Configuration;

import com.google.gson.Gson;

public class PingSender {

	private static final String PING_INFO_INPUT_NAME = "pingInfo";

	private static final Gson gson = new Gson();

	private Configuration config;

	public PingSender() {
		config = Agent.get().getConfig();
	}

	public PingInfo ping(String url, boolean noProxy) throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();

		PingInfo myPingInfo = new PingInfo(Agent.get().getAgentId());

		try {

			RequestConfig.Builder requestConfigBuilder = RequestConfig.custom().setSocketTimeout(3000).setConnectTimeout(3000);

			if (!noProxy && config.isHttpProxy()) {
				requestConfigBuilder.setProxy(new HttpHost(config.getHttpProxyHost(), config.getHttpProxyPort()));
			}

			HttpUriRequest ping = RequestBuilder.post(url)
					.setConfig(requestConfigBuilder.build())
					.addParameter(PING_INFO_INPUT_NAME, gson.toJson(myPingInfo))
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
