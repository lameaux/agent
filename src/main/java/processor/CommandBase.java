package processor;

import org.restexpress.Request;

public abstract class CommandBase implements Command {

	public static final String COMMAND_SEPARATOR = " ";

	public abstract String name();

	public String execute(String request) {
		return null;
	}

	public String execute(Request request) {
		return null;
	}

	public String help() {
		return name();
	}

	public boolean match(String request) {
		if (nullOrEmpty(request)) {
			return false;
		}

		return name().equalsIgnoreCase(request) || request.toLowerCase().startsWith(name() + COMMAND_SEPARATOR);
	}

	public String syntaxError() {
		return "Syntax error. Help: " + help();
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

	public boolean nullOrEmpty(String value) {
		return value == null || value.trim().isEmpty();
	}

}
