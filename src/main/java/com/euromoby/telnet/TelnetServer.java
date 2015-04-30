package com.euromoby.telnet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.Config;
import com.euromoby.service.NettyService;

@Component
public class TelnetServer extends NettyService {

	public static final String SERVICE_NAME = "telnet";
	public static final int TELNET_PORT = 23;
	
	private Config config;

	@Autowired
	public TelnetServer(Config config, TelnetServerInitializer initializer) {
		super(initializer);
		this.config = config;
	}

	@Override
	public int getPort() {
		return config.getTelnetPort();
	}
	
	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}

}
