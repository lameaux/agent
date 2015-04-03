package com.euromoby.processor;

import org.springframework.stereotype.Component;

import com.euromoby.utils.StringUtils;

@Component
public class SysInfoCommand extends CommandBase implements Command {

	public static final String NAME = "sysinfo";
	
	@Override
	public String execute(String request) {

		String[] params = parameters(request);
		if (params.length == 1 && !StringUtils.nullOrEmpty(params[0])) {
			String param = params[0];
			return StringUtils.printProperties(System.getProperties(), param);			
		}

		return StringUtils.printProperties(System.getProperties(), null);
	}

	@Override
	public String help() {
		return NAME + "\t\t\tlist system parameters" + StringUtils.CRLF +
				NAME + "\t<parameter>\tshow parameter value";
	}	
	
	@Override
	public String name() {
		return NAME;
	}

}
