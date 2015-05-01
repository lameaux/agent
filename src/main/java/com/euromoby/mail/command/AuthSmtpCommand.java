package com.euromoby.mail.command;

import org.springframework.stereotype.Component;

@Component
public class AuthSmtpCommand extends SmtpCommandBase implements SmtpCommand {

	public static final String COMMAND_NAME = "AUTH";

	@Override
	public String name() {
		return COMMAND_NAME;
	}

}
