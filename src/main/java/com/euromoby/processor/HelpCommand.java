package com.euromoby.processor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.utils.StringUtils;

@Component
public class HelpCommand extends CommandBase implements Command {

	public static final String NAME = "help";	
	public static final String COMMAND_NOT_FOUND = "Command not found";
	public static final String HELP_HEADER = "Supported commands:";
	public static final String HELP_FOOTER = "Type \"" + NAME + " <command>\" for the list of parameters";
	
	private List<Command> commands;

	@Autowired
	public void setCommands(List<Command> commands) {
		this.commands = commands;
	}
	
	@Override
	public String execute(String request) {
		
		String[] params = parameters(request);
		if (params.length == 1 && !StringUtils.nullOrEmpty(params[0])) {
			for (Command command : commands) {
				if (command.name().equalsIgnoreCase(params[0])) {
					return command.help();
				}
			}
			return COMMAND_NOT_FOUND;
		} else {		
			StringBuffer sb = new StringBuffer();
			sb.append(HELP_HEADER + StringUtils.CRLF);
			for (Command command : commands) {
				sb.append(command.name()).append(StringUtils.CRLF);
			}
			sb.append(StringUtils.CRLF + HELP_FOOTER + StringUtils.CRLF);			
			return sb.toString();
		}
	}

	@Override
	public String help() {
		return NAME + "\t\t\tlist of available commands" + StringUtils.CRLF +
				NAME + "\t<command>\tshow help for the command";
	}	
	
	@Override
	public String name() {
		return NAME;
	}

}
