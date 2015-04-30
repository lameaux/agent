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

	public String[] parameters(String request) {

		String nameWithSeparator = name() + SEPARATOR;
		if (!request.startsWith(nameWithSeparator)) {
			return new String[0];
		}

		String[] params = request.substring(nameWithSeparator.length()).trim().split(SEPARATOR);
		// trim
		for (int i = 0; i < params.length; i++) {
			params[i] = params[i].trim();
		}
		return params;
	}

}
