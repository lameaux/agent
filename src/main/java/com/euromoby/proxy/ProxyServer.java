package com.euromoby.proxy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.Config;
import com.euromoby.service.NettyService;

@Component
public class ProxyServer extends NettyService {
	
	public static final String SERVICE_NAME = "proxy";
	public static final int PROXY_PORT = 3128;
	
	private Config config;
	
	@Autowired
	public ProxyServer(Config config, ProxyServerInitializer initializer) {
		super(initializer);
		this.config = config;
	}


	@Override
	public int getPort() {
		return config.getBasePort() + PROXY_PORT;
	}

	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}

}