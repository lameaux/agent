package com.euromoby.cdn;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
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

	@Autowired
	public CdnNetwork(Config config, AgentManager agentManager, HttpClientProvider httpClientProvider) {
		this.config = config;
		this.agentManager = agentManager;
		this.httpClientProvider = httpClientProvider;

		executor = Executors.newFixedThreadPool(this.config.getCdnPoolSize());
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

	public String requestSourceDownload(URI uri) {
		if (!isAvailable(uri.getPath())) {
			return null;
		}		
		return null;
	}
	
	public String findSourceUrl(URI uri) {
		return null;
	}
	
	public FileInfo find(String uriPath) {
		if (!isAvailable(uriPath)) {
			return null;
		}
		// send requests to active agents
		List<AgentId> activeAgents = agentManager.getActive();
		List<Future<FileInfo>> futureList = new ArrayList<Future<FileInfo>>();
		for (AgentId agentId : activeAgents) {
			Future<FileInfo> future = executor.submit(new CdnWorker(httpClientProvider, agentId, uriPath));
			futureList.add(future);
		}
		
		// lets wait 500 ms
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return null;
		}		
		// gather results from agents with timeout
		long endTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(config.getCdnTimeout());
		List<FileInfo> fileInfoResult = new ArrayList<FileInfo>();
		while (endTime > System.currentTimeMillis()) {
			Iterator<Future<FileInfo>> futureIterator = futureList.iterator();
			if (!futureIterator.hasNext()) {
				break;
			}
			while (futureIterator.hasNext()) {
				Future<FileInfo> future = futureIterator.next();
				if (future.isDone()) {
					try {
						FileInfo fileInfo = future.get();
						if (fileInfo != null) {
							fileInfoResult.add(fileInfo);
						}
						futureIterator.remove();
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						break;
					} catch (ExecutionException e) {
						futureIterator.remove();
					}
				}
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
		}
		
		if (fileInfoResult.isEmpty()) {
			return null;
		}
		
		// TODO choose best source
		return fileInfoResult.get(0);
	}

}
