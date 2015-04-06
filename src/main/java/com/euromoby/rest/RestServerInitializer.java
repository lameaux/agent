package com.euromoby.rest;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

import com.euromoby.agent.Config;
import com.euromoby.http.AgentHttpResponseEncoder;
import com.euromoby.http.AuthenticationHandler;
import com.euromoby.http.SSLContextProvider;

public class RestServerInitializer extends ChannelInitializer<SocketChannel> {
	
	private final Config config;
	private final SSLContextProvider sslContextProvider;	
	private final RestMapper restMapper;

	public RestServerInitializer(Config config, SSLContextProvider sslContextProvider, RestMapper restMapper) {
		this.config = config;
		this.sslContextProvider = sslContextProvider;
		this.restMapper = restMapper;
	}

	@Override
	public void initChannel(SocketChannel ch) {
		ChannelPipeline p = ch.pipeline();
		
		p.addLast("ssl", new SslHandler(sslContextProvider.newServerSSLEngine()));		
		p.addLast("decoder", new HttpRequestDecoder());
		p.addLast("encoder", new AgentHttpResponseEncoder());
		p.addLast("auth", new AuthenticationHandler(config));
		//TODO IdleStateHandler
		p.addLast("chunked", new ChunkedWriteHandler());
		p.addLast("rest", new RestServerHandler(restMapper));
	}
}