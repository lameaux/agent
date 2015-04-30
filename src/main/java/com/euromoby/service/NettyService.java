package com.euromoby.service;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class NettyService implements Service {

	private static final Logger LOG = LoggerFactory.getLogger(NettyService.class);

	private volatile ServiceState serviceState = ServiceState.STOPPED;

	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	private Channel serverChannel;	
	
	protected ChannelInitializer<SocketChannel> initializer;
	
	public NettyService(ChannelInitializer<SocketChannel> initializer) {
		this.initializer = initializer;
	}	
	
	public abstract int getPort();

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

			serverChannel = b.bind(getPort()).sync().channel();

			serviceState = ServiceState.RUNNING;
			LOG.info("{} started on port {}", getServiceName(), getPort());
		} catch (Exception e) {
			LOG.error("Error starting " + getServiceName() + " on port " + getPort(), e);
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
			LOG.info("{} stopped", getServiceName());
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
		if (serviceState == ServiceState.RUNNING) {
			return;
		}		
		Thread thread = new Thread(this);
		thread.start();
			try {
				thread.join();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
	}

	@Override
	public void stopService() {
		shutdown();
	}

	@Override
	public ServiceState getServiceState() {
		return serviceState;
	}

}
