package rest;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import org.restexpress.RestExpress;
import org.restexpress.pipeline.SimpleConsoleLogMessageObserver;
import org.restexpress.serialization.GsonSerializationProvider;

import rest.handler.EchoHandler;
import rest.handler.HtmlHandler;
import service.Service;
import service.ServiceState;

public class RestServer implements Service {

	private static GsonSerializationProvider jsonSerialization = new GsonSerializationProvider();

	public static final String SERVICE_NAME = "rest";

	private final int port;
	private RestExpress server;

	private volatile ServiceState serviceState = ServiceState.STOPPED;

	private SslContext sslCtx;

	public RestServer(int port) {
		this(port, false);
	}

	public RestServer(int port, boolean ssl) {
		this.port = port;

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

			RestExpress.setSerializationProvider(jsonSerialization);

			server = new RestExpress();
			server.setName("Agent Rest Server");
			server.addMessageObserver(new SimpleConsoleLogMessageObserver());

			registerRoutes();

			server.bind(port);

			serviceState = ServiceState.RUNNING;
		} catch (Exception e) {
			shutdown();
		}
	}

	private void registerRoutes() {
		server.uri("/echo", new EchoHandler()).method(HttpMethod.GET);
		server.uri("/html", new HtmlHandler()).method(HttpMethod.GET).noSerialization();
	}

	public void shutdown() {
		if (server != null) {
			server.shutdown();
		}
		serviceState = ServiceState.STOPPED;
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
