package com.euromoby.rest;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestServerInitializer extends ChannelInitializer<SocketChannel> {

	private static final Logger LOG = LoggerFactory.getLogger(RestServerInitializer.class);	
	
	private final SslEngineFactory sslEngineFactory;	
	private final RestMapper restMapper;

	public RestServerInitializer(SslEngineFactory sslEngineFactory, RestMapper restMapper) {
		this.sslEngineFactory = sslEngineFactory;
		this.restMapper = restMapper;
	}

	@Override
	public void initChannel(SocketChannel ch) {
		ChannelPipeline p = ch.pipeline();
		
		if (sslEngineFactory != null) {
			try {
				p.addLast(new SslHandler(sslEngineFactory.newSslEngine()));
			} catch (Exception e) {
				LOG.error("SSL initialization failed", e);
			}			
		}
		
		p.addLast("decoder", new HttpRequestDecoder());
		p.addLast("encoder", new AgentHttpResponseEncoder());
		//IdleStateHandler
		p.addLast("chunked", new ChunkedWriteHandler());
		p.addLast("rest", new RestServerHandler(restMapper));
	}
}