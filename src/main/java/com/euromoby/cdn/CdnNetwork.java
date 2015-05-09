package com.euromoby.cdn;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.AgentManager;
import com.euromoby.agent.Config;
import com.euromoby.http.HttpClientProvider;
import com.euromoby.model.AgentId;
import com.euromoby.model.Tuple;
import com.euromoby.rest.handler.fileinfo.FileInfo;

@Component
public class CdnNetwork {

	private static final Logger log = LoggerFactory.getLogger(CdnNetwork.class);
	
	private Config config;
	private AgentManager agentManager;
	private HttpClientProvider httpClientProvider;
	private CdnResourceMapping cdnResourceMapping;

	private ExecutorService executor;
	private ExecutorCompletionService<FileInfo> completionService;

	@Autowired
	public CdnNetwork(Config config, AgentManager agentManager, HttpClientProvider httpClientProvider, CdnResourceMapping cdnResourceMapping) {
		this.config = config;
		this.agentManager = agentManager;
		this.httpClientProvider = httpClientProvider;
		this.cdnResourceMapping = cdnResourceMapping;

		executor = Executors.newFixedThreadPool(this.config.getCdnPoolSize());
		completionService = new ExecutorCompletionService<FileInfo>(executor);
		
	}

	protected int sendRequestsToActiveAgents(String uriPath) {
		List<AgentId> activeAgents = agentManager.getActive();
		for (AgentId agentId : activeAgents) {
			completionService.submit(new CdnWorker(httpClientProvider, agentId, uriPath));
		}
		return activeAgents.size();
	}
	
	protected List<FileInfo> getResponsesFromAgents(int agentCount) {
		List<FileInfo> fileInfoResult = new ArrayList<FileInfo>();
		
		long timeoutMillis = config.getCdnTimeout();
		long endTime = System.currentTimeMillis() + timeoutMillis;
		
		while (agentCount > 0) {
			long timeout = Math.max(0, endTime - System.currentTimeMillis());
			try {
				Future<FileInfo> future = completionService.poll(timeout, TimeUnit.MILLISECONDS);
				if (future == null) {
					break;
				}
				agentCount--;
				FileInfo fileInfo = future.get();
				if (fileInfo != null) {
					fileInfoResult.add(fileInfo);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			} catch (ExecutionException e) {
				log.debug("Request failed", e);
				continue;
			}
		}
		
		return fileInfoResult;
	}
	
	protected FileInfo chooseBestSource(List<FileInfo> fileInfoList) {
		if (fileInfoList.isEmpty()) {
			return null;
		}
		for (FileInfo fileInfo : fileInfoList) {
			if (fileInfo.isComplete()) {
				return fileInfo;
			}
		}
		return null;
	}

	
	public Tuple<CdnResource, FileInfo> find(String uriPath) {
		
		Tuple<CdnResource, FileInfo> searchResult = Tuple.empty();
		
		CdnResource cdnResource = cdnResourceMapping.findByUrl(uriPath);
		if (cdnResource == null) {
			return searchResult;
		}
		searchResult.setFirst(cdnResource);

		if (cdnResource.isAvailableInNetwork()) {
			int agentCount = sendRequestsToActiveAgents(uriPath);
			List<FileInfo> fileInfoResult = getResponsesFromAgents(agentCount);
			FileInfo fileInfo = chooseBestSource(fileInfoResult);
			searchResult.setSecond(fileInfo);
		}

		return searchResult;
	}

}
