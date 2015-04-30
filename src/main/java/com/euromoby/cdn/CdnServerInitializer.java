package com.euromoby.cdn;

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
import com.euromoby.download.DownloadScheduler;
import com.euromoby.file.FileProvider;
import com.euromoby.file.MimeHelper;
import com.euromoby.http.AgentHttpResponseEncoder;
import com.euromoby.http.SmartHttpContentCompressor;
import com.euromoby.network.ReadWriteTimeoutHandler;
import com.euromoby.proxy.ProxyResponseProvider;

@Component
public class CdnServerInitializer extends ChannelInitializer<SocketChannel> {

	private Config config;
	private FileProvider fileProvider;
	private MimeHelper mimeHelper;
	private CdnNetwork cdnNetwork;
	private DownloadScheduler downloadScheduler;
	private ProxyResponseProvider proxyResponseProvider;
	
	@Autowired
	public CdnServerInitializer(Config config, FileProvider fileProvider, MimeHelper mimeHelper, CdnNetwork cdnNetwork, DownloadScheduler downloadScheduler, ProxyResponseProvider proxyResponseProvider) {
		this.config = config;
		this.fileProvider = fileProvider;
		this.mimeHelper = mimeHelper;
		this.cdnNetwork = cdnNetwork;
		this.downloadScheduler = downloadScheduler;
		this.proxyResponseProvider = proxyResponseProvider;
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
		p.addLast("cdn", new CdnServerHandler(fileProvider, mimeHelper, cdnNetwork, downloadScheduler, proxyResponseProvider));
	}
}