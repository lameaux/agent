package com.euromoby.rest.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;

import java.net.URI;

public interface RestHandler {
	void process(ChannelHandlerContext ctx, HttpRequest request, HttpPostRequestDecoder decoder);
	boolean matchUri(URI uri);
}
