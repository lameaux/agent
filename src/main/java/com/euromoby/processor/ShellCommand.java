package com.euromoby.processor;

import org.springframework.stereotype.Component;

import com.euromoby.utils.ShellExecutor;
import com.euromoby.utils.StringUtils;

@Component
public class ShellCommand extends CommandBase implements Command {

	public static final String NAME = "shell";		
	public static final long TIMEOUT = 5 * 60 * 1000; // 5 minutes
	
	private ShellExecutor shellExecutor;
	
	public ShellCommand(ShellExecutor shellExecutor) {
		this.shellExecutor = shellExecutor;
	}

	@Override
	public String execute(String request) {

		String[] params = parameters(request);
		if (params.length == 0 || StringUtils.nullOrEmpty(params[0])) {
			return syntaxError();
		}

		try {
			return shellExecutor.executeCommandLine(params, TIMEOUT);
		} catch (Exception e) {
			return e.getMessage();
		}

	}

	@Override
	public String help() {
		return NAME + "\t<command>\texecute shell command";
	}

	@Override
	public String name() {
		return NAME;
	}

}
