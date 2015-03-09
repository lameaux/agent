package processor;

public class PingCommand extends CommandBase implements Command {

	public String execute(String request) {

		String[] params = parameters(request);
		if (params.length == 0 || nullOrEmpty(params[0])) {
			return syntaxError();
		}

		return "pong " + params[0];
	}

	@Override
	public boolean match(String request) {
		return request.toLowerCase().startsWith("ping");
	}

	@Override
	public String help() {
		return "ping example.com";
	}

	@Override
	public String name() {
		return "ping";
	}

}
