package com.euromoby.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.Config;
import com.euromoby.service.NettyService;

@Component
public class RestServer extends NettyService {

	public static final String SERVICE_NAME = "rest";
	public static final int REST_PORT = 443;

	private Config config;

	@Autowired
	public RestServer(Config config, RestServerInitializer initializer) {
		super(initializer);
		this.config = config;
	}

	@Override
	public int getPort() {
		return config.getBasePort() + REST_PORT;
	}

	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}

}