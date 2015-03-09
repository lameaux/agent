package processor;

import java.util.ArrayList;
import java.util.List;

public class CommandProcessor {

	List<Command> commands = new ArrayList<Command>();

	public CommandProcessor() {
		commands.add(new TimeCommand());
		commands.add(new UptimeCommand());
	}

	public String process(String request) {
		for (Command command : commands) {
			if (command.match(request)) {
				return command.execute(request);
			}
		}
		return "Invalid command";
	}

}
