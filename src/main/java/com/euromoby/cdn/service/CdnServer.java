package com.euromoby.cdn.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.Config;
import com.euromoby.service.NettyService;

@Component
public class CdnServer extends NettyService {
	
	public static final String SERVICE_NAME = "cdn";
	public static final int CDN_PORT = 80;
	
	private Config config;
	
	@Autowired
	public CdnServer(Config config, CdnServerInitializer initializer) {
		super(initializer);
		this.config = config;
	}


	@Override
	public int getPort() {
		return config.getCdnPort();
	}

	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}

}