package processor;

public abstract class CommandBase implements Command {

	public abstract String name();

	public String help() {
		return name();
	}

	public boolean match(String request) {
		return name().equalsIgnoreCase(request);
	}

	public String syntaxError() {
		return "Syntax error. Help: " + help();
	}

	public String[] parameters(String request) {

		String[] params = request.substring(name().length()).trim().split(" ");
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
