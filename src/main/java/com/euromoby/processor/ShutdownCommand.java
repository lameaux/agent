package com.euromoby.processor;

import org.springframework.stereotype.Component;

import com.euromoby.agent.Agent;

@Component
public class ShutdownCommand extends CommandBase implements Command {

	public static final String NAME = "shutdown";
	public static final String GOODBYE = "Shutting down...";
	
	@Override
	public String execute(String request) {
		Agent.shutdown();
		return GOODBYE;
	}

	@Override
	public String help() {
		return NAME + "\t\tterminate Agent instance";
	}	
	
	@Override
	public String name() {
		return NAME;
	}

}
