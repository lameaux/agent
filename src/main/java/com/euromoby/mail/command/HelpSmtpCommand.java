package com.euromoby.mail.command;

import org.springframework.stereotype.Component;

@Component
public class HelpSmtpCommand extends SmtpCommandBase implements SmtpCommand {

	public static final String COMMAND_NAME = "HELP";

	@Override
	public String name() {
		return COMMAND_NAME;
	}

}
