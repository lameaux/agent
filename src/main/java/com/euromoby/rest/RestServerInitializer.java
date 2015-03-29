package com.euromoby.rest;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;

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
			p.addLast("ssl", sslCtx.newHandler(ch.alloc()));
		}
		p.addLast("decoder", new HttpRequestDecoder());
		p.addLast("encoder", new AgentHttpResponseEncoder());
		//IdleStateHandler
		p.addLast("compressor", new SmartHttpContentCompressor());
		p.addLast("chunked", new ChunkedWriteHandler());
		p.addLast("rest", new RestServerHandler(restMapper));
	}
}