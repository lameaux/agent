package processor;

import java.util.ArrayList;
import java.util.List;

public class CommandProcessor {

	private List<Command> commands = new ArrayList<Command>();

	public CommandProcessor() {
		commands.add(new HelpCommand(commands));
		commands.add(new TimeCommand());
		commands.add(new WhoCommand());
		commands.add(new UptimeCommand());
		commands.add(new ShellCommand());
		commands.add(new VersionCommand());
		commands.add(new ConfigCommand());
		commands.add(new SysInfoCommand());
		commands.add(new InetAddressCommand());
		commands.add(new ServiceCommand());
		commands.add(new PingCommand());
		commands.add(new AgentsCommand());
		commands.add(new DownloadCommand());		
		commands.add(new UploadCommand());
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
