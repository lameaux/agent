package processor;

import utils.ShellExecutor;


public class ShellCommand extends CommandBase implements Command {

	private static final long TIMEOUT = 60 * 1000; // 1 minute
	
	public String execute(String request) {

		String[] params = parameters(request);
		if (params.length == 0 || nullOrEmpty(params[0])) {
			return syntaxError();
		}

		try {
			return ShellExecutor.executeCommandLine(params, TIMEOUT);
		} catch (Exception e) {
			return e.getMessage();
		}
		
	}

	@Override
	public String help() {
		return "shell echo Hello";
	}

	@Override
	public String name() {
		return "shell";
	}

}
