package com.euromoby.proxy;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import org.springframework.stereotype.Component;

import com.euromoby.http.HttpUtils;

@Component
public class ProxyResponseProvider {

	public void proxy(ChannelHandlerContext ctx, FullHttpRequest httpRequest, String sourceUrl) {
		// TODO real proxying (AsyncClient)
		
		// Build the response object.
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.GATEWAY_TIMEOUT, HttpUtils.fromString(HttpResponseStatus.GATEWAY_TIMEOUT.reasonPhrase()));
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
		// Write the response.
		ChannelFuture future = ctx.channel().writeAndFlush(response);
		future.addListener(ChannelFutureListener.CLOSE);		
	}
	
}
