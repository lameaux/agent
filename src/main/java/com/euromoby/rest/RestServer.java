package com.euromoby.rest;

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
public class RestServer implements Service {

	public static final String SERVICE_NAME = "rest";
	public static final int REST_PORT = 443;
	
	private Config config;
	private RestMapper restMapper; 
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	private volatile ServiceState serviceState = ServiceState.STOPPED;

	private Channel serverChannel;
	private SSLContextProvider sslContextProvider;

	private static final Logger LOG = LoggerFactory.getLogger(RestServer.class); 
	
	@Autowired
	public RestServer(Config config, SSLContextProvider sslContextProvider, RestMapper restMapper) {
		this.config = config;
		this.restMapper = restMapper;
		this.sslContextProvider = sslContextProvider;
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
			b.childHandler(new RestServerInitializer(sslContextProvider, restMapper));

			serverChannel = b.bind(config.getRestPort()).sync().channel();

			serviceState = ServiceState.RUNNING;
			LOG.info("RestServer started on port {}", config.getRestPort());
		} catch (Exception e) {
			LOG.error("Error starting RestServer on port " + config.getRestPort(), e);
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
			LOG.info("RestServer stopped");
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