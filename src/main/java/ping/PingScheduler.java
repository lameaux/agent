package ping;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import model.AgentId;
import model.PingInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import service.Service;
import service.ServiceState;
import agent.Agent;
import agent.AgentManager;
import agent.Configuration;

public class PingScheduler implements Service {

	public static final String SERVICE_NAME = "ping";

	private static final int SLEEP_TIME = 1000;

	private static final Logger LOG = LoggerFactory.getLogger(PingScheduler.class);

	private volatile ServiceState serviceState = ServiceState.STOPPED;
	private volatile boolean interrupted = false;

	private AgentManager agentManager;

	private Configuration config;
	private ExecutorService executor;
	private ExecutorCompletionService<PingInfo> completionService;

	public PingScheduler() {
		config = Agent.get().getConfig();
		agentManager = Agent.get().getAgentManager();

		executor = Executors.newFixedThreadPool(config.getJobPoolSize());
		completionService = new ExecutorCompletionService<PingInfo>(executor);
	}

	@Override
	public void run() {

		interrupted = false;
		LOG.info("PingScheduler started");

		while (!interrupted) {

			// check for received pings
			Future<PingInfo> pingInfoFuture = completionService.poll();
			if (pingInfoFuture != null) {
				try {
					agentManager.notifyPingSendSuccess(pingInfoFuture.get());
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					interrupted = true;
					break;
				} catch (ExecutionException e) {
					LOG.warn("Pinged with error", e.getCause());
				}
			}

			// check for new pings
			List<AgentId> agentsToPing = agentManager.getAllForPing();
			for (AgentId agentId : agentsToPing) {
				completionService.submit(new PingWorker(agentId));
				agentManager.notifyPingSendAttempt(agentId);
			}

			// sleep
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
				interrupted = true;
				break;
			}

		}
		serviceState = ServiceState.STOPPED;
		LOG.info("PingScheduler stopped");
	}

	@Override
	public void startService() {
		serviceState = ServiceState.RUNNING;
		new Thread(this).start();
	}

	@Override
	public void stopService() {
		interrupted = true;
		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
		}
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
