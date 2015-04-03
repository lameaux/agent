package com.euromoby.processor;

import org.springframework.stereotype.Component;

import com.euromoby.utils.DateUtils;

@Component
public class TimeCommand extends CommandBase implements Command {

	public static final String NAME = "time";
	
	@Override
	public String execute(String request) {
		return DateUtils.iso(System.currentTimeMillis());
	}

	@Override
	public String help() {
		return NAME + "\t\tshow current time (UTC)";
	}	
	
	@Override
	public String name() {
		return NAME;
	}

}
