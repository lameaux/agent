package com.euromoby.mail.command;

import org.springframework.stereotype.Component;

@Component
public class ExpnSmtpCommand extends SmtpCommandBase implements SmtpCommand {

	public static final String COMMAND_NAME = "EXPN";

	@Override
	public String name() {
		return COMMAND_NAME;
	}

}
