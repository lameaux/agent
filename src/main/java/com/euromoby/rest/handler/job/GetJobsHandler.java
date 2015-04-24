package com.euromoby.rest.handler.job;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.AgentManager;
import com.euromoby.http.HttpResponseProvider;
import com.euromoby.job.JobDetail;
import com.euromoby.model.AgentId;
import com.euromoby.rest.RestException;
import com.euromoby.rest.handler.RestHandlerBase;
import com.google.gson.Gson;

@Component
public class GetJobsHandler extends RestHandlerBase {

	public static final String URL = "/getjobs";
	public static final String AGENT_ID_PARAM_NAME = "agentId";

	private static final Logger LOG = LoggerFactory.getLogger(GetJobsHandler.class);
	private static final Gson gson = new Gson();

	private AgentManager agentManager;
	
	@Autowired
	public GetJobsHandler(AgentManager agentManager) {
		this.agentManager = agentManager;
	}
	
	@Override
	public boolean matchUri(URI uri) {
		return uri.getPath().equals(URL);
	}
	
	@Override
	public FullHttpResponse doGet() throws Exception {
		return createJobsResponse();
	}

	private AgentId getAgentId() throws Exception {
		Map<String, List<String>> getParameters = getUriAttributes();
		List<String> agentIdList = getParameters.get(AGENT_ID_PARAM_NAME);
		if (agentIdList == null || agentIdList.isEmpty()) {
			throw new RestException("agentId is missing");
		}
		return new AgentId(agentIdList.get(0));
	}
	
	private FullHttpResponse createJobsResponse() throws Exception {

		AgentId agentId = getAgentId();		
		JobDetail[] jobDetails = new JobDetail[]{};
		
		String jsonResponse = gson.toJson(jobDetails);
		ByteBuf content = Unpooled.copiedBuffer(jsonResponse, CharsetUtil.UTF_8);
		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
		FullHttpResponse response = httpResponseProvider.createHttpResponse(HttpResponseStatus.OK, content);
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json; charset=UTF-8");
		return response;		
	}
	
}
