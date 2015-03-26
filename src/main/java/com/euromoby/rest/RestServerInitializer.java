package com.euromoby.rest;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.ssl.SslContext;

public class RestServerInitializer extends ChannelInitializer<SocketChannel> {

	private final SslContext sslCtx;
	private final RestMapper restMapper;

	public RestServerInitializer(SslContext sslCtx, RestMapper restMapper) {
		this.sslCtx = sslCtx;
		this.restMapper = restMapper;

	}

	@Override
	public void initChannel(SocketChannel ch) {
		ChannelPipeline p = ch.pipeline();
		if (sslCtx != null) {
			p.addLast(sslCtx.newHandler(ch.alloc()));
		}
		p.addLast(new HttpRequestDecoder());
		p.addLast(new AgentHttpResponseEncoder());
		p.addLast(new RestServerHandler(restMapper));
	}
}