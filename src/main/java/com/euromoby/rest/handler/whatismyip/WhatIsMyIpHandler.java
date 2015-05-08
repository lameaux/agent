package com.euromoby.rest.handler.whatismyip;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.euromoby.http.HttpResponseProvider;
import com.euromoby.http.HttpUtils;
import com.euromoby.rest.handler.RestHandlerBase;

@Component
public class WhatIsMyIpHandler extends RestHandlerBase {

	public static final String URL = "/whatismyip";

	@Override
	public boolean matchUri(URI uri) {
		return uri.getPath().equals(URL);
	}	
	
	@Override
	public FullHttpResponse doGet(ChannelHandlerContext ctx, HttpRequest request, Map<String, List<String>> queryParameters) {
		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
		FullHttpResponse response = httpResponseProvider.createHttpResponse(HttpResponseStatus.OK, HttpUtils.fromString(getClientInetAddress(ctx).getHostAddress()));
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
		return response;
	}

	protected InetAddress getClientInetAddress(ChannelHandlerContext ctx) {
		return ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress();		
	}
}
