package com.euromoby.processor;

import org.springframework.stereotype.Component;

import com.euromoby.agent.Agent;

@Component
public class VersionCommand extends CommandBase implements Command {

	public static final String NAME = "version";
	public static final String OUTPUT = "%s %s";

	@Override
	public String execute(String request) {
		return String.format(OUTPUT, Agent.TITLE, Agent.VERSION);
	}

	@Override
	public String help() {
		return NAME + "\t\tshow Agent version";
	}

	@Override
	public String name() {
		return NAME;
	}

}
