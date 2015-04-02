package com.euromoby.processor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommandProcessor {

	public static final String INVALID_COMMAND = "Invalid command";
	
	private List<Command> commands;

	@Autowired
	public void setCommands(List<Command> commands) {
		this.commands = commands;
	}

	public String process(String request) {
		try {
			for (Command command : commands) {
				if (command.match(request)) {
					return command.execute(request);
				}
			}
			return INVALID_COMMAND;
		} catch (Exception e) {
			return "Error: " + e.getMessage();
		}
	}

}
