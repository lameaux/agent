package com.euromoby.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.Config;
import com.euromoby.utils.SystemUtils;

@Component
public class FreeSpaceCommand extends CommandBase implements Command {
	
	public static final String NAME = "freespace";
	
	private Config config;
	
	@Autowired
	public FreeSpaceCommand(Config config) {
		this.config = config;
	}	
	
	@Override
	public String execute(String request) {
		long sizeInMegabytes = SystemUtils.getFreeSpace(config.getAgentFilesPath()) / (1024*1024);
		return  String.format("%dG %dM", sizeInMegabytes / 1024, sizeInMegabytes % 1024) ;
	}

	@Override
	public String help() {
		return NAME + "\t\tget free space";
	}	
	
	@Override
	public String name() {
		return NAME;
	}

}
