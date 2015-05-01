package com.euromoby.processor;

import org.springframework.stereotype.Component;

@Component
public class ExitCommand extends CommandBase implements Command {

	public static final String NAME = "exit";

	@Override
	public String execute(String request) {
		return "Bye!";
	}

	@Override
	public String help() {
		return NAME + "\t\tclose session";
	}

	@Override
	public String name() {
		return NAME;
	}

}
