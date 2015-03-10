package telnet;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.concurrent.Future;
import service.Service;
import service.ServiceState;

public class TelnetServer extends Thread implements Service {

	public static final String SERVICE_NAME = "telnet";
	
	private final int port;
	private final EventLoopGroup bossGroup;
	private final EventLoopGroup workerGroup;
	private volatile ServiceState serviceState = ServiceState.STOPPED;

	private Channel serverChannel;
	private SslContext sslCtx;

	public TelnetServer(int port) {
		this(port, false);
	}

	public TelnetServer(int port, boolean ssl) {
		this.port = port;
		bossGroup = new NioEventLoopGroup(1);
		workerGroup = new NioEventLoopGroup();

		if (ssl) {
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
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup);
			b.channel(NioServerSocketChannel.class);
			b.handler(new LoggingHandler(LogLevel.INFO));
			b.childHandler(new TelnetServerInitializer(sslCtx));

			serverChannel = b.bind(port).sync().channel();
			serverChannel.closeFuture();
			
			serviceState = ServiceState.RUNNING;
		} catch (Exception e) {
			shutdown();
		}
	}


	public void shutdown() {
		try {
			if (serverChannel != null) {
				serverChannel.close().sync();
			}
		} catch(Exception e) {
			
		} finally {
			shutdownWorkers();
			serviceState = ServiceState.STOPPED;
		}
	}	
	
	private void shutdownWorkers() {
		Future<?> fb = bossGroup.shutdownGracefully();
		Future<?> fw = workerGroup.shutdownGracefully();
		try {
			fb.await();
			fw.await();
		} catch (InterruptedException ignore) {
		}
	}

	public void startService() {
		this.start();
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
