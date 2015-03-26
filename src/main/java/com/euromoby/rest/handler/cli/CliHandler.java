package com.euromoby.rest.handler.cli;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;


import com.euromoby.agent.Agent;
import com.euromoby.processor.CommandProcessor;
import com.euromoby.rest.handler.RestHandlerBase;
import com.euromoby.utils.IOUtils;
import com.google.gson.Gson;

public class CliHandler extends RestHandlerBase {

	public static final String URL = "/cli";

	private static final String REQUEST_INPUT_NAME = "request";

	protected final CommandProcessor commandProcessor;

	public CliHandler() {
		this.commandProcessor = Agent.get().getCommandProcessor();
	}

	@Override
	public FullHttpResponse doGet() {
		InputStream is = CliHandler.class.getResourceAsStream("cli.html");
		ByteBuf content = Unpooled.copiedBuffer(IOUtils.streamToString(is), CharsetUtil.UTF_8);
		return createHttpResponse(HttpResponseStatus.OK, content);
	}

	@Override
	public FullHttpResponse doPost() throws IOException {

		Map<String, String> requestParameters = getRequestParameters();

		String message = commandProcessor.process(requestParameters.get(REQUEST_INPUT_NAME));

		Gson gson = new Gson();
		String jsonResponse = gson.toJson(new CliResponse(message));
		ByteBuf content = Unpooled.copiedBuffer(jsonResponse, CharsetUtil.UTF_8);
		FullHttpResponse response = createHttpResponse(HttpResponseStatus.OK, content);
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json; charset=UTF-8");
		return response;
	}

}
