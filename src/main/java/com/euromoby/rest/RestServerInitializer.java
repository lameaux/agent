package com.euromoby.rest;

import com.euromoby.agent.SSLContextProvider;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

public class RestServerInitializer extends ChannelInitializer<SocketChannel> {
	
	private final SSLContextProvider sslContextProvider;	
	private final RestMapper restMapper;

	public RestServerInitializer(SSLContextProvider sslContextProvider, RestMapper restMapper) {
		this.sslContextProvider = sslContextProvider;
		this.restMapper = restMapper;
	}

	@Override
	public void initChannel(SocketChannel ch) {
		ChannelPipeline p = ch.pipeline();
		
		p.addLast("ssl", new SslHandler(sslContextProvider.newServerSSLEngine()));		
		p.addLast("decoder", new HttpRequestDecoder());
		p.addLast("encoder", new AgentHttpResponseEncoder());
		//IdleStateHandler
		p.addLast("chunked", new ChunkedWriteHandler());
		p.addLast("rest", new RestServerHandler(restMapper));
	}
}