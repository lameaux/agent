package com.euromoby.rest.handler.welcome;

import com.euromoby.agent.Agent;
import com.euromoby.rest.handler.RestHandlerBase;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

public class WelcomeHandler extends RestHandlerBase {

	public static final String URL = "/";	
	
	@Override
	public FullHttpResponse doGet() {

		// response.setContentType("text/html");
		StringBuilder sb = new StringBuilder();
		sb.append("<!DOCTYPE html><html><head><title>");
		sb.append(getWelcomeString());
		sb.append("</title></head><body>");
		sb.append(getWelcomeString());
		sb.append("</body></html>");

		ByteBuf content = Unpooled.copiedBuffer(sb.toString(), CharsetUtil.UTF_8);
		return createHttpResponse(HttpResponseStatus.OK, content);
	}

	private String getWelcomeString() {
		return Agent.TITLE + " " + Agent.VERSION;
	}
}
