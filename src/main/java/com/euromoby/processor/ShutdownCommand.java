package com.euromoby.processor;

import org.springframework.stereotype.Component;

import com.euromoby.agent.Agent;

@Component
public class ShutdownCommand extends CommandBase implements Command {

	@Override
	public String execute(String request) {
		Agent.shutdown();
		return "Shutting down...";
	}

	@Override
	public String help() {
		return "shutdown\t\tterminate Agent instance";
	}	
	
	@Override
	public String name() {
		return "shutdown";
	}

}
