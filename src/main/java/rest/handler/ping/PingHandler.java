package rest.handler.ping;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.util.Map;

import model.PingInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rest.handler.RestHandlerBase;
import agent.Agent;

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
	public FullHttpResponse doPost() throws IOException {
		Map<String, String> requestParameters = getRequestParameters();
		String pingInfo = requestParameters.get(PING_INFO_INPUT_NAME);
		if (pingInfo == null) {
			return createHttpResponse(HttpResponseStatus.BAD_REQUEST);
		}
		LOG.debug("Received Ping message {} from {}", pingInfo, getClientInetAddress().getHostAddress());
		
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
