package com.euromoby.processor;

import com.euromoby.agent.Agent;

public class VersionCommand extends CommandBase implements Command {

	@Override
	public String execute(String request) {
		return Agent.TITLE + " " + Agent.VERSION;
	}

	@Override
	public String help() {
		return "version\t\tshow Agent version";
	}	
	
	@Override
	public String name() {
		return "version";
	}

}
