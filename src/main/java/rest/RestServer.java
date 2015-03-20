package rest;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import service.Service;
import service.ServiceState;
import agent.Agent;
import agent.Configuration;

public class RestServer implements Service {

	public static final String SERVICE_NAME = "rest";

	private final Configuration config;
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	private volatile ServiceState serviceState = ServiceState.STOPPED;

	private Channel serverChannel;
	private SslContext sslCtx;

	private static final Logger LOG = LoggerFactory.getLogger(RestServer.class); 
	
	public RestServer() {
		this.config = Agent.get().getConfig();

		if (config.isRestSsl()) {
			try {
				SelfSignedCertificate ssc = new SelfSignedCertificate();
				sslCtx = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
			} catch (Exception e) {
				sslCtx = null;
			}
		} else {
			sslCtx = null;
		}

	}

	public void run() {

		try {

			bossGroup = new NioEventLoopGroup(1);
			workerGroup = new NioEventLoopGroup();

			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup);
			b.channel(NioServerSocketChannel.class);
			b.handler(new LoggingHandler(LogLevel.INFO));
			b.childHandler(new RestServerInitializer(sslCtx));

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

	public void startService() {
		new Thread(this).start();
	}

	public void stopService() {
		shutdown();
	}

	public String getServiceName() {
		return SERVICE_NAME;
	}

	public ServiceState getServiceState() {
		return serviceState;
	}

}