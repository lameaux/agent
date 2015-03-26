package processor;

import utils.StringUtils;


public abstract class CommandBase implements Command {

	public static final String COMMAND_SEPARATOR = " ";

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

		return name().equalsIgnoreCase(request) || request.toLowerCase().startsWith(name() + COMMAND_SEPARATOR);
	}

	public String syntaxError() {
		return "Syntax error. Help: " + StringUtils.CRLF + help();
	}

	public String[] parameters(String request) {

		String nameWithSeparator = name() + COMMAND_SEPARATOR;
		if (!request.startsWith(nameWithSeparator)) {
			return new String[0];
		}

		String[] params = request.substring(nameWithSeparator.length()).trim().split(COMMAND_SEPARATOR);
		// trim
		for (int i = 0; i < params.length; i++) {
			params[i] = params[i].trim();
		}
		return params;
	}



}
