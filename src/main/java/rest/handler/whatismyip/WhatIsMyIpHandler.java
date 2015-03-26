package rest.handler.whatismyip;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import rest.handler.RestHandlerBase;

public class WhatIsMyIpHandler extends RestHandlerBase {

	public static final String URL = "/whatismyip";

	@Override
	public FullHttpResponse doGet() {
		FullHttpResponse response = createHttpResponse(HttpResponseStatus.OK, fromString(getClientInetAddress().getHostAddress()));
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
		return response;
	}

}
