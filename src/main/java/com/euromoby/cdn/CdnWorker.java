package com.euromoby.cdn;

import java.util.concurrent.Callable;

import com.euromoby.agent.Config;
import com.euromoby.http.HttpClientProvider;
import com.euromoby.model.AgentId;
import com.euromoby.rest.handler.fileinfo.FileInfo;


public class CdnWorker implements Callable<FileInfo> {

	private AgentId agentId;
	private Config config;
	private HttpClientProvider httpClientProvider;

	public CdnWorker(AgentId agentId, Config config, HttpClientProvider httpClientProvider) {
		this.agentId = agentId;
		this.config = config;
		this.httpClientProvider = httpClientProvider;
	}

	@Override
	public FileInfo call() throws Exception {
		return null;
	}	
	
}
