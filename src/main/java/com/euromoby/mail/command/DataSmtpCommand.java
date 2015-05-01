package com.euromoby.mail.command;

import org.springframework.stereotype.Component;

import com.euromoby.mail.MailSession;
import com.euromoby.mail.util.DSNStatus;
import com.euromoby.model.Tuple;

@Component
public class DataSmtpCommand extends SmtpCommandBase implements SmtpCommand {

	public static final String COMMAND_NAME = "DATA";

	public static final String RESPONSE_503_NO_RECIPIENTS = "503 "+DSNStatus.getStatus(DSNStatus.PERMANENT,DSNStatus.DELIVERY_OTHER)+" No recipients specified";
	public static final String RESPONSE_354_OK = "354 Ok Send data ending with <CRLF>.<CRLF>";
	
	@Override
	public String name() {
		return COMMAND_NAME;
	}

	@Override
	public String execute(MailSession mailSession, Tuple<String, String> request) {
		if (mailSession.getRecipient().isEmpty()) {
			return RESPONSE_503_NO_RECIPIENTS;
		}
		mailSession.setCommandMode(false);
		return RESPONSE_354_OK;
	}

}
