package ping;

import model.AgentId;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import agent.Agent;

import com.google.gson.Gson;

public class PingSender {

	private static final String PING_URL = "/ping";
	private static final String AGENT_ID_INPUT_NAME = "agentId";

	private static final Gson gson = new Gson();

	private CloseableHttpClient httpclient;

	public PingSender() {
		httpclient = HttpClients.createMinimal();
	}

	public AgentId ping(String hostname, int restPort) throws Exception {
		AgentId myAgentId = Agent.get().getAgentId();

		HttpUriRequest ping = RequestBuilder.post("http://" + hostname + ":" + restPort + PING_URL)
				.addParameter(AGENT_ID_INPUT_NAME, gson.toJson(myAgentId)).build();

		CloseableHttpResponse response = httpclient.execute(ping);
		try {
			HttpEntity entity = response.getEntity();
			String content = EntityUtils.toString(entity);
			EntityUtils.consumeQuietly(entity);
			return gson.fromJson(content, AgentId.class);
		} finally {
			response.close();
		}

	}
}
