package com.euromoby.proxy;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.Config;
import com.euromoby.http.AgentHttpResponseEncoder;
import com.euromoby.http.AsyncHttpClientProvider;
import com.euromoby.http.SmartHttpContentCompressor;
import com.euromoby.network.ReadWriteTimeoutHandler;

@Component
public class ProxyServerInitializer extends ChannelInitializer<SocketChannel> {

	private Config config;
	private AsyncHttpClientProvider asyncHttpClientProvider;

	@Autowired
	public ProxyServerInitializer(Config config, AsyncHttpClientProvider asyncHttpClientProvider) {
		this.config = config;
		this.asyncHttpClientProvider = asyncHttpClientProvider;
	}

	@Override
	public void initChannel(SocketChannel ch) {
		ChannelPipeline p = ch.pipeline();

		p.addLast("decoder", new HttpRequestDecoder());
		p.addLast("encoder", new AgentHttpResponseEncoder());
		p.addLast("aggregator", new HttpObjectAggregator(65536));

		p.addLast("idle", new IdleStateHandler(0, 0, config.getServerTimeout()));
		p.addLast("timeout", new ReadWriteTimeoutHandler());

		p.addLast("compressor", new SmartHttpContentCompressor());
		p.addLast("chunked", new ChunkedWriteHandler());
		p.addLast("cdn", new ProxyServerHandler(config, asyncHttpClientProvider));
	}
}