package processor;

import java.util.List;

public class HelpCommand extends CommandBase implements Command {

	private static final String CRLF = "\r\n";

	private List<Command> commands;

	public HelpCommand(List<Command> commands) {
		this.commands = commands;
	}

	public String execute(String request) {
		StringBuffer sb = new StringBuffer();
		sb.append("Supported commands:" + CRLF);
		for (Command command : commands) {
			sb.append(command.help()).append(CRLF);
		}
		return sb.toString();
	}

	@Override
	public String name() {
		return "help";
	}

}
