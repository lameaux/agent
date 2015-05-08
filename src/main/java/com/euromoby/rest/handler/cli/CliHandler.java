package com.euromoby.rest.handler.cli;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.http.HttpResponseProvider;
import com.euromoby.processor.CommandProcessor;
import com.euromoby.rest.handler.RestHandlerBase;
import com.euromoby.utils.IOUtils;
import com.euromoby.utils.ListUtils;
import com.google.gson.Gson;

@Component
public class CliHandler extends RestHandlerBase {

	public static final String URL = "/cli";
	private static final String REQUEST_INPUT_NAME = "request";

	private final CommandProcessor commandProcessor;

	@Autowired
	public CliHandler(CommandProcessor commandProcessor) {
		this.commandProcessor = commandProcessor;
	}

	@Override
	public boolean matchUri(URI uri) {
		return uri.getPath().equals(URL);
	}
	
	@Override
	public FullHttpResponse doGet(ChannelHandlerContext ctx, HttpRequest request, Map<String, List<String>> queryParameters) {
		InputStream is = CliHandler.class.getResourceAsStream("cli.html");
		ByteBuf content = Unpooled.copiedBuffer(IOUtils.streamToString(is), CharsetUtil.UTF_8);
		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
		return httpResponseProvider.createHttpResponse(HttpResponseStatus.OK, content);
	}

	@Override
	public FullHttpResponse doPost(ChannelHandlerContext ctx, HttpRequest request, Map<String, List<String>> queryParameters, Map<String, List<String>> postParameters, Map<String, File> uploadFiles) throws IOException {

		String message = commandProcessor.process(ListUtils.getFirst(postParameters.get(REQUEST_INPUT_NAME)));

		Gson gson = new Gson();
		String jsonResponse = gson.toJson(new CliResponse(message));
		ByteBuf content = Unpooled.copiedBuffer(jsonResponse, CharsetUtil.UTF_8);
		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
		FullHttpResponse response = httpResponseProvider.createHttpResponse(HttpResponseStatus.OK, content);
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json; charset=UTF-8");
		return response;
	}

}
