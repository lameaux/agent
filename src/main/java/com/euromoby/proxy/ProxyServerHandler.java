package com.euromoby.proxy;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import com.euromoby.agent.Config;
import com.euromoby.http.AsyncHttpClientProvider;
import com.euromoby.http.HttpUtils;

public class ProxyServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

	private Config config;
	private AsyncHttpClientProvider asyncHttpClientProvider;

	public ProxyServerHandler(Config config, AsyncHttpClientProvider asyncHttpClientProvider) {
		this.config = config;
		this.asyncHttpClientProvider = asyncHttpClientProvider;
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
		writeErrorResponse(ctx, HttpResponseStatus.GATEWAY_TIMEOUT);
	}

	protected void writeErrorResponse(ChannelHandlerContext ctx, HttpResponseStatus status) {
		writeErrorResponse(ctx, status, status.reasonPhrase());
	}

	protected void writeErrorResponse(ChannelHandlerContext ctx, HttpResponseStatus status, String message) {
		// Build the response object.
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, HttpUtils.fromString(message));
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
		// Write the response.
		ChannelFuture future = ctx.channel().writeAndFlush(response);
		future.addListener(ChannelFutureListener.CLOSE);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.channel().close();
	}

}