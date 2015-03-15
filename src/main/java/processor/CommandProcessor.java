package processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import service.ServiceManager;

public class CommandProcessor {

	private List<Command> commands = new ArrayList<Command>();
	private Map<String, Object> context = new HashMap<String, Object>();

	public CommandProcessor(ServiceManager serviceManager) {
		commands.add(new HelpCommand(commands));
		commands.add(new TimeCommand());
		commands.add(new WhoCommand());
		commands.add(new UptimeCommand());
		commands.add(new ShellCommand());
		commands.add(new VersionCommand());
		commands.add(new ConfigCommand());
		commands.add(new SysInfoCommand());
		commands.add(new HostnameCommand());
		commands.add(new ServiceCommand(serviceManager));
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
