package com.euromoby.mail.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.service.NettyService;

@Component
public class MailServer extends NettyService {

	public static final String SERVICE_NAME = "mail";
	public static final int MAIL_PORT = 25;
	
	@Autowired
	public MailServer(MailServerInitializer initializer) {
		super(initializer);
	}

	@Override
	public int getPort() {
		return MAIL_PORT;
	}
	
	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}

}
