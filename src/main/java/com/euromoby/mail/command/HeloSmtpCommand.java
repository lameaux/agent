package com.euromoby.mail.command;

import org.springframework.stereotype.Component;

import com.euromoby.mail.MailSession;
import com.euromoby.model.Tuple;
import com.euromoby.utils.StringUtils;

@Component
public class HeloSmtpCommand extends SmtpCommandBase implements SmtpCommand {

	public static final String COMMAND_NAME = "HELO";

	public static final String RESPONSE_501_INVALID_DOMAIN = "501 Invalid domain name";
	public static final String RESPONSE_250_HELLO = "250 Hello ";
	
	@Override
	public String name() {
		return COMMAND_NAME;
	}

	@Override
	public String execute(MailSession mailSession, Tuple<String, String> request) {
		String domain = request.getSecond();
		if (StringUtils.nullOrEmpty(domain)) {
			return RESPONSE_501_INVALID_DOMAIN;
		}
		mailSession.setDomain(domain);
		return RESPONSE_250_HELLO + domain;
	}

}
