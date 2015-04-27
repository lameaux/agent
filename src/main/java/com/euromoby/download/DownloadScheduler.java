package com.euromoby.download;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.AgentManager;

@Component
public class DownloadScheduler {

	private AgentManager agentManager;
	
	@Autowired
	public DownloadScheduler(AgentManager agentManager) {
		this.agentManager = agentManager;
		
	}
	
	public void addDownloadRequest(String url, String fileLocation) {
		// TODO distribute - send request to better agent
	}

}
