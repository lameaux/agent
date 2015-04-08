package com.euromoby.processor;

import com.euromoby.utils.StringUtils;

public abstract class CommandBase implements Command {

	public static final String SYNTAX_ERROR = "Syntax error. Help: ";
	
	@Override
	public abstract String name();

	@Override
	public String execute(String request) {
		return null;
	}

	@Override
	public String help() {
		return name();
	}

	@Override
	public boolean match(String request) {
		if (StringUtils.nullOrEmpty(request)) {
			return false;
		}

		return name().equalsIgnoreCase(request) || request.toLowerCase().startsWith(name() + SEPARATOR);
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
