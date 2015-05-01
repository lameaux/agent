package com.euromoby.mail.command;

import org.springframework.stereotype.Component;

import com.euromoby.mail.MailSession;
import com.euromoby.mail.util.DSNStatus;
import com.euromoby.model.Tuple;

@Component
public class QuitSmtpCommand extends SmtpCommandBase implements SmtpCommand {

	public static final String COMMAND_NAME = "QUIT";
	public static final String RESPONSE_221_BYE = "221 " + DSNStatus.getStatus(DSNStatus.SUCCESS, DSNStatus.UNDEFINED_STATUS) + " Bye!";

	@Override
	public String name() {
		return COMMAND_NAME;
	}

	@Override
	public String execute(MailSession mailSession, Tuple<String, String> request) {
		return RESPONSE_221_BYE;
	}

}
