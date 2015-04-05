package com.euromoby.cdn;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.AgentManager;
import com.euromoby.agent.Config;
import com.euromoby.http.HttpClientProvider;
import com.euromoby.model.AgentId;
import com.euromoby.rest.handler.fileinfo.FileInfo;

@Component
public class CdnNetwork {

	private Config config;
	private AgentManager agentManager;
	private HttpClientProvider httpClientProvider;

	private ExecutorService executor;
	private ExecutorCompletionService<FileInfo> completionService;

	@Autowired
	public CdnNetwork(Config config, AgentManager agentManager, HttpClientProvider httpClientProvider) {
		this.config = config;
		this.agentManager = agentManager;
		this.httpClientProvider = httpClientProvider;

		executor = Executors.newFixedThreadPool(this.config.getCdnPoolSize());
		completionService = new ExecutorCompletionService<FileInfo>(executor);
	}

	private boolean isAvailable(String uriPath) {
		if (uriPath.startsWith("/thumb/")) {
			return true;
		}
		if (uriPath.startsWith("/video/")) {
			return true;
		}
		return false;
	}

	public FileInfo find(String uriPath) {
		if (!isAvailable(uriPath)) {
			return null;
		}

		List<AgentId> activeAgents = agentManager.getActive();
		List<Future<FileInfo>> futureList = new ArrayList<Future<FileInfo>>();
		for (AgentId agentId : activeAgents) {
			Future<FileInfo> future = executor.submit(new CdnWorker(agentId, config, httpClientProvider));
			futureList.add(future);
		}
		long endTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(config.getCdnTimeout());
		while (endTime > System.currentTimeMillis()) {
			for (Future<FileInfo> future : futureList) {
				if (future.isDone()) {
					try {
						FileInfo fileInfo = future.get();
						return fileInfo;
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						return null;
					} catch (ExecutionException e) {
						// ignore;
					}
				}
			}
		}

		return null;
	}

}
