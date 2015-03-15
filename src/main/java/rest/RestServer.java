package rest;

import io.netty.handler.codec.http.HttpMethod;

import org.restexpress.RestExpress;
import org.restexpress.pipeline.SimpleConsoleLogMessageObserver;
import org.restexpress.serialization.GsonSerializationProvider;

import processor.CommandProcessor;
import rest.handler.CliHandler;
import rest.handler.WelcomeHandler;
import service.Service;
import service.ServiceState;

public class RestServer implements Service {

	private static GsonSerializationProvider jsonSerialization = new GsonSerializationProvider();

	public static final String SERVICE_NAME = "rest";

	private final int port;
	private final CommandProcessor commandProcessor;
	private RestExpress server;

	private volatile ServiceState serviceState = ServiceState.STOPPED;

	public RestServer(int port, CommandProcessor commandProcessor) {
		this.port = port;
		this.commandProcessor = commandProcessor;
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
		// welcome
		server.uri("/", new WelcomeHandler(commandProcessor)).method(HttpMethod.GET).noSerialization();
		// command line interface
		CliHandler cliHandler = new CliHandler(commandProcessor);
		server.uri("/cli", cliHandler).method(HttpMethod.GET).noSerialization();
		server.uri("/cli", cliHandler).method(HttpMethod.POST);
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
