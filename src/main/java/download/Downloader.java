package download;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import agent.Agent;
import agent.Configuration;

public class Downloader {

	private Configuration config;

	public Downloader() {
		config = Agent.get().getConfig();
	}

	public void download(String url, String location, boolean noProxy) throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {

			RequestConfig.Builder requestConfigBuilder = RequestConfig.custom().setSocketTimeout(3000).setConnectTimeout(3000);

			if (!noProxy && config.isHttpProxy()) {
				requestConfigBuilder.setProxy(new HttpHost(config.getHttpProxyHost(), config.getHttpProxyPort()));
			}

			HttpGet request = new HttpGet(url);
			request.setConfig(requestConfigBuilder.build());

			CloseableHttpResponse response = httpclient.execute(request);
			try {
				EntityUtils.consume(response.getEntity());
			} finally {
				response.close();
			}

		} finally {
			httpclient.close();
		}
	}

}
