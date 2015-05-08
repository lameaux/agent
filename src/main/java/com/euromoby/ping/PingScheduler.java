package com.euromoby.ping;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.AgentManager;
import com.euromoby.agent.Config;
import com.euromoby.model.AgentId;
import com.euromoby.model.PingInfo;
import com.euromoby.service.SchedulerService;

@Component
public class PingScheduler extends SchedulerService {

	public static final String SERVICE_NAME = "ping";

	private static final Logger LOG = LoggerFactory.getLogger(PingScheduler.class);

	private Config config;	
	private AgentManager agentManager;
	private PingSender pingSender;

	private ExecutorService executor;
	private ExecutorCompletionService<PingInfo> completionService;

	@Autowired
	public PingScheduler(Config config, AgentManager agentManager, PingSender pingSender) {
		this.config = config;
		this.agentManager = agentManager;
		this.pingSender = pingSender;

		executor = Executors.newFixedThreadPool(this.config.getPingPoolSize());
		completionService = new ExecutorCompletionService<PingInfo>(executor);
	}

	@Override
	public void executeInternal() throws InterruptedException {
		checkReceivedPings();
		scheduleNextPings();
	}

	protected void checkReceivedPings() throws InterruptedException {
		// check for received pings
		Future<PingInfo> pingInfoFuture = completionService.poll();
		if (pingInfoFuture != null) {
			try {
				agentManager.notifyPingSendSuccess(pingInfoFuture.get());
			} catch (ExecutionException e) {
				LOG.debug("Pinged with error: {}", e.getMessage());
			}
		}
	}

	protected void scheduleNextPings() {
		// check for new pings
		List<AgentId> agentsToPing = agentManager.getAllForPing();
		for (AgentId agentId : agentsToPing) {
			completionService.submit(new PingWorker(agentId, pingSender));
			agentManager.notifyPingSendAttempt(agentId);
		}
	}

	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}

}
