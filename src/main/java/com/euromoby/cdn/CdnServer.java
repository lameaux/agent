package com.euromoby.cdn;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.Config;
import com.euromoby.service.Service;
import com.euromoby.service.ServiceState;

@Component
public class CdnServer implements Service {
	
	public static final String SERVICE_NAME = "cdn";
	public static final int CDN_PORT = 80;
	
	private Config config;
	private CdnServerInitializer initializer;
	
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	private volatile ServiceState serviceState = ServiceState.STOPPED;

	private Channel serverChannel;

	private static final Logger LOG = LoggerFactory.getLogger(CdnServer.class); 
	
	@Autowired
	public CdnServer(Config config, CdnServerInitializer initializer) {
		this.config = config;
		this.initializer = initializer;
	}

	@Override
	public void run() {

		try {
			bossGroup = new NioEventLoopGroup(1);
			workerGroup = new NioEventLoopGroup();

			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup);
			b.channel(NioServerSocketChannel.class);
			b.handler(new LoggingHandler(LogLevel.INFO));
			b.childHandler(initializer);

			serverChannel = b.bind(config.getCdnPort()).sync().channel();

			serviceState = ServiceState.RUNNING;
			LOG.info("CdnServer started on port {}", config.getCdnPort());
		} catch (Exception e) {
			LOG.error("Error starting CdnServer on port " + config.getCdnPort(), e);
			shutdown();
		}
	}

	public void shutdown() {
		try {
			if (serverChannel != null) {
				serverChannel.close().sync();
			}
		} catch (Exception e) {

		} finally {
			shutdownWorkers();
			serviceState = ServiceState.STOPPED;
			LOG.info("CdnServer stopped");
		}
	}

	private void shutdownWorkers() {
		if (bossGroup != null) {
			bossGroup.shutdownGracefully();
			bossGroup = null;
		}
		if (workerGroup != null) {
			workerGroup.shutdownGracefully();
			workerGroup = null;
		}
	}

	@Override
	public void startService() {
		new Thread(this).start();
	}

	@Override
	public void stopService() {
		shutdown();
	}

	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}

	@Override
	public ServiceState getServiceState() {
		return serviceState;
	}

}