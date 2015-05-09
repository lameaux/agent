package com.euromoby.ping;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.Config;
import com.euromoby.ping.model.PingInfo;
import com.euromoby.utils.SystemUtils;

@Component
public class PingInfoProvider {
	
	private Config config;
	
	@Autowired
	public PingInfoProvider(Config config) {
		this.config = config;
	}	
	
	public PingInfo createPingInfo() {
		PingInfo pingInfo = new PingInfo(config.getAgentId());
		pingInfo.setFreeSpace(SystemUtils.getFreeSpace(config.getAgentFilesPath()));
		return pingInfo;
	}
}
