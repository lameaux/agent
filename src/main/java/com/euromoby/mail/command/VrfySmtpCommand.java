package com.euromoby.mail.command;

import org.springframework.stereotype.Component;

@Component
public class VrfySmtpCommand extends SmtpCommandBase implements SmtpCommand {

	public static final String COMMAND_NAME = "VRFY";

	@Override
	public String name() {
		return COMMAND_NAME;
	}

}
