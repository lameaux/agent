package telnet;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import processor.CommandProcessor;
import service.Service;
import service.ServiceState;

public class TelnetServer implements Service {

	public static final String SERVICE_NAME = "telnet";

	private final int port;
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	private volatile ServiceState serviceState = ServiceState.STOPPED;

	private Channel serverChannel;
	private CommandProcessor commandProcessor;

	private static final Logger LOG = LoggerFactory.getLogger(TelnetServer.class); 	
	
	public TelnetServer(int port, CommandProcessor commandProcessor) {
		this.port = port;
		this.commandProcessor = commandProcessor;
	}

	public void run() {

		try {

			bossGroup = new NioEventLoopGroup(1);
			workerGroup = new NioEventLoopGroup();

			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup);
			b.channel(NioServerSocketChannel.class);
			b.handler(new LoggingHandler(LogLevel.INFO));
			b.childHandler(new TelnetServerInitializer(commandProcessor));

			serverChannel = b.bind(port).sync().channel();

			serviceState = ServiceState.RUNNING;
			LOG.info("TelnetServer started on port {}", port);
		} catch (Exception e) {
			LOG.error("Error starting TelnetServer on port " + port, e);
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
