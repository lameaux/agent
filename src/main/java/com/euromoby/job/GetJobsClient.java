package com.euromoby.job;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.Config;
import com.euromoby.agent.SSLContextProvider;
import com.euromoby.model.AgentId;
import com.euromoby.rest.RestServer;
import com.euromoby.rest.handler.job.GetJobsHandler;
import com.euromoby.utils.HttpUtils;
import com.google.gson.Gson;

@Component
public class GetJobsClient {

	private static final Gson gson = new Gson();

	private Config config;
	private SSLContextProvider sslContextProvider;

	@Autowired
	public GetJobsClient(Config config, SSLContextProvider sslContextProvider) {
		this.config = config;
		this.sslContextProvider = sslContextProvider;
	}

	public JobDetail[] getJobs(AgentId agentId, boolean noProxy) throws Exception {
		CloseableHttpClient httpclient = HttpUtils.defaultHttpClient(sslContextProvider.getSSLContext());

		AgentId myAgentId = config.getAgentId();
		try {

			RequestConfig.Builder requestConfigBuilder = HttpUtils.createRequestConfigBuilder(config, noProxy);
			String url = "https://" + agentId.getHost() + ":" + (agentId.getBasePort() + RestServer.REST_PORT) + GetJobsHandler.URL;
			HttpUriRequest request = RequestBuilder.get(url).setConfig(requestConfigBuilder.build())
					.addParameter(GetJobsHandler.AGENT_ID_PARAM_NAME, myAgentId.toString()).build();

			CloseableHttpResponse response = httpclient.execute(request);

			try {
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
					EntityUtils.consumeQuietly(response.getEntity());
					throw new Exception(statusLine.getStatusCode() + " " + statusLine.getReasonPhrase());
				}

				HttpEntity entity = response.getEntity();
				String content = EntityUtils.toString(entity);
				EntityUtils.consumeQuietly(entity);
				JobDetail[] jobDetails = gson.fromJson(content, JobDetail[].class);
				return jobDetails;
			} finally {
				response.close();
			}
		} finally {
			httpclient.close();
		}
	}

}
