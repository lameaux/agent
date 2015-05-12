package com.euromoby.cdn.service;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.AgentManager;
import com.euromoby.agent.Config;
import com.euromoby.cdn.CdnNetwork;
import com.euromoby.download.DownloadManager;
import com.euromoby.file.FileProvider;
import com.euromoby.file.MimeHelper;
import com.euromoby.http.AgentHttpResponseEncoder;
import com.euromoby.http.AsyncHttpClientProvider;
import com.euromoby.http.SmartHttpContentCompressor;
import com.euromoby.network.ReadWriteTimeoutHandler;

@Component
public class CdnServerInitializer extends ChannelInitializer<SocketChannel> {

	private Config config;
	private FileProvider fileProvider;
	private MimeHelper mimeHelper;
	private CdnNetwork cdnNetwork;
	private DownloadManager downloadManager;
	private AgentManager agentManager;
	private AsyncHttpClientProvider asyncHttpClientProvider;
	
	@Autowired
	public CdnServerInitializer(Config config, FileProvider fileProvider, MimeHelper mimeHelper, CdnNetwork cdnNetwork, DownloadManager downloadManager, AgentManager agentManager, AsyncHttpClientProvider asyncHttpClientProvider) {
		this.config = config;
		this.fileProvider = fileProvider;
		this.mimeHelper = mimeHelper;
		this.cdnNetwork = cdnNetwork;
		this.downloadManager = downloadManager;
		this.agentManager = agentManager;
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
		p.addLast("cdn", new CdnServerHandler(config, fileProvider, mimeHelper, cdnNetwork, downloadManager, agentManager, asyncHttpClientProvider));
	}
}