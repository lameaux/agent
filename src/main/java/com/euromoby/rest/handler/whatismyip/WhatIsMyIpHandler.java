package com.euromoby.rest.handler.whatismyip;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.net.URI;

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
	public FullHttpResponse doGet() {
		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
		FullHttpResponse response = httpResponseProvider.createHttpResponse(HttpResponseStatus.OK, HttpUtils.fromString(getClientInetAddress().getHostAddress()));
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
		return response;
	}

}
