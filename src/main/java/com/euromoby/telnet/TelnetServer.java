package com.euromoby.telnet;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.euromoby.agent.Agent;
import com.euromoby.agent.Config;
import com.euromoby.service.Service;
import com.euromoby.service.ServiceState;


public class TelnetServer implements Service {

	public static final String SERVICE_NAME = "telnet";
	public static final int TELNET_PORT = 23;
	
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	private volatile ServiceState serviceState = ServiceState.STOPPED;

	private Channel serverChannel;
	private Config config;

	private static final Logger LOG = LoggerFactory.getLogger(TelnetServer.class); 	
	
	public TelnetServer() {
		this.config = Agent.get().getConfig();
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
			b.childHandler(new TelnetServerInitializer());

			serverChannel = b.bind(config.getTelnetPort()).sync().channel();

			serviceState = ServiceState.RUNNING;
			LOG.info("TelnetServer started on port {}", config.getTelnetPort());
		} catch (Exception e) {
			LOG.error("Error starting TelnetServer on port " + config.getTelnetPort(), e);
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
			LOG.info("TelnetServer stopped");
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
