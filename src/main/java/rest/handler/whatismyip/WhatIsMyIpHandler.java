package rest.handler.whatismyip;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import rest.handler.RestHandlerBase;

public class WhatIsMyIpHandler extends RestHandlerBase {

	public static final String URL = "/whatismyip";

	@Override
	public FullHttpResponse doGet() {
		return createHttpResponse(HttpResponseStatus.OK, fromString(getClientInetAddress().getHostAddress()));
	}

}
