package com.euromoby.cdn;

import org.springframework.stereotype.Component;

import com.euromoby.model.AgentId;

@Component
public class CdnNetwork {

	public AgentId find(String fileLocation) {
		if (fileLocation.startsWith("img/")) {
			return new AgentId("dirty.to:0");
		}
		return null;
	}

}
