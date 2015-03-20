package rest.handler.ping;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rest.handler.RestHandlerBase;
import utils.NetUtils;
import agent.Agent;

import com.google.gson.Gson;

public class PingHandler extends RestHandlerBase {

	public static final String URL = "/ping";

	private static final String HOSTNAME_INPUT_NAME = "hostname";
	private static final String VERSION_INPUT_NAME = "version";

	private static final Logger LOG = LoggerFactory.getLogger(PingHandler.class);

	// POST
	@Override
	public FullHttpResponse doGet() {

		Gson gson = new Gson();
		String jsonResponse = gson.toJson(new PingResponse(NetUtils.getHostname(), Agent.VERSION));
		ByteBuf content = Unpooled.copiedBuffer(jsonResponse, CharsetUtil.UTF_8);
		FullHttpResponse response = createHttpResponse(HttpResponseStatus.OK, content);
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json; charset=UTF-8");
		return response;
	}

	// POST
	@Override
	public FullHttpResponse doPost() throws IOException {

		Map<String, String> requestParameters = getRequestParameters();

		String hostName = requestParameters.get(HOSTNAME_INPUT_NAME);
		String version = requestParameters.get(VERSION_INPUT_NAME);

		LOG.debug("Received Ping message from {} running Agent {}", hostName, version);

		return createHttpResponse();
	}

}
