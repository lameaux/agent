package processor;

import utils.ShellExecutor;
import utils.StringUtils;

public class ShellCommand extends CommandBase implements Command {

	private static final long TIMEOUT = 60 * 1000; // 1 minute

	@Override
	public String execute(String request) {

		String[] params = parameters(request);
		if (params.length == 0 || StringUtils.nullOrEmpty(params[0])) {
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
		return "shell\t<command>\texecute shell command";
	}

	@Override
	public String name() {
		return "shell";
	}

}
