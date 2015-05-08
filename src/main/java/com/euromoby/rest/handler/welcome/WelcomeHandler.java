package com.euromoby.rest.handler.welcome;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.euromoby.agent.Agent;
import com.euromoby.http.HttpResponseProvider;
import com.euromoby.rest.handler.RestHandlerBase;
import com.euromoby.utils.IOUtils;

@Component
public class WelcomeHandler extends RestHandlerBase {

	public static final String URL = "/";	

	@Override
	public boolean matchUri(URI uri) {
		return uri.getPath().equals(URL);
	}	
	
	@Override
	public FullHttpResponse doGet(ChannelHandlerContext ctx, HttpRequest request, Map<String, List<String>> queryParameters) {
		InputStream is = WelcomeHandler.class.getResourceAsStream("welcome.html");
		String pageContent = IOUtils.streamToString(is);
		pageContent = pageContent.replace("%AGENT%", Agent.TITLE);
		pageContent = pageContent.replace("%VERSION%", Agent.VERSION);
		ByteBuf content = Unpooled.copiedBuffer(pageContent, CharsetUtil.UTF_8);
		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
		return httpResponseProvider.createHttpResponse(HttpResponseStatus.OK, content);		
	}
}
