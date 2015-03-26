package com.euromoby.processor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.utils.StringUtils;

@Component
public class HelpCommand extends CommandBase implements Command {

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
			return "Command not found";
		} else {		
			StringBuffer sb = new StringBuffer();
			sb.append("Supported commands:" + StringUtils.CRLF);
			for (Command command : commands) {
				sb.append(command.name()).append(StringUtils.CRLF);
			}
			sb.append(StringUtils.CRLF + "Type \"help <command>\" for the list of parameters" + StringUtils.CRLF);			
			return sb.toString();
		}
		
	}

	@Override
	public String help() {
		return "help\t\t\tlist of available commands" + StringUtils.CRLF +
				"help\t<command>\tshow help for the command";
	}	
	
	@Override
	public String name() {
		return "help";
	}

}
