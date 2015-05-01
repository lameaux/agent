package com.euromoby.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MailManager {

	private static final Logger log = LoggerFactory.getLogger(MailManager.class);
	
	public void save(MailSession mailSession) {
		log.info("Mail size {}", mailSession.getRealMailSize());
		log.info("Headers:");
		for (String header : mailSession.getHeaders()) {
			log.info(header);
		}
		log.info("Body:");
		for (String body : mailSession.getBody()) {
			log.info(body);
		}
	}
	
}
