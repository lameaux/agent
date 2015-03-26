package com.euromoby.rest.handler.ping;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import java.util.Map;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.euromoby.agent.Agent;
import com.euromoby.model.PingInfo;
import com.euromoby.rest.RestException;
import com.euromoby.rest.handler.RestHandlerBase;
import com.google.gson.Gson;

public class PingHandler extends RestHandlerBase {

	public static final String URL = "/ping";
	private static final String PING_INFO_INPUT_NAME = "pingInfo";

	private static final Logger LOG = LoggerFactory.getLogger(PingHandler.class);
	private static final Gson gson = new Gson();
	
	@Override
	public FullHttpResponse doGet() {
		return createPingResponse();
	}

	@Override
	public FullHttpResponse doPost() throws RestException {
		Map<String, String> requestParameters = getRequestParameters();
		String pingInfoString = requestParameters.get(PING_INFO_INPUT_NAME);
		if (pingInfoString == null) {
			throw new RestException("Parameter is missing: " + PING_INFO_INPUT_NAME);
		}
		
		PingInfo pingInfo = gson.fromJson(pingInfoString, PingInfo.class);
		if (pingInfo.getAgentId() == null) {
			throw new RestException("AgentId is missing");
		}
		
		// set real host address
		pingInfo.getAgentId().setHost(getClientInetAddress().getHostAddress());
		// notify received ping
		Agent.get().getAgentManager().notifyPingReceive(pingInfo);
		
		LOG.debug("Received Ping message from {}", pingInfo.getAgentId());
		
		return createPingResponse();
	}

	private FullHttpResponse createPingResponse() {
		String jsonResponse = gson.toJson(new PingInfo(Agent.get().getAgentId()));
		ByteBuf content = Unpooled.copiedBuffer(jsonResponse, CharsetUtil.UTF_8);
		FullHttpResponse response = createHttpResponse(HttpResponseStatus.OK, content);
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json; charset=UTF-8");
		return response;		
	}
	
}
