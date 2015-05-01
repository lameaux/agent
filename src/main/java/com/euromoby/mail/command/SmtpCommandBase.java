package com.euromoby.mail.command;

import com.euromoby.mail.MailSession;
import com.euromoby.model.Tuple;
import com.euromoby.utils.StringUtils;

public abstract class SmtpCommandBase implements SmtpCommand {

	public static final String SYNTAX_ERROR = "500 Syntax error. Help: ";
	
	@Override
	public abstract String name();

	@Override
	public String execute(MailSession mailSession, Tuple<String, String> request) {
		return null;
	}

	@Override
	public String help() {
		return name();
	}

	@Override
	public boolean match(Tuple<String, String> request) {
		if (StringUtils.nullOrEmpty(request.getFirst())) {
			return false;
		}

		return name().equalsIgnoreCase(request.getFirst());
	}

	public String syntaxError() {
		return SYNTAX_ERROR + StringUtils.CRLF + help();
	}


}
