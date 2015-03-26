package processor;

import utils.StringUtils;


public class SysInfoCommand extends CommandBase implements Command {

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
		return "sysinfo\t\t\tlist system parameters" + StringUtils.CRLF +
				"sysinfo\t<parameter>\tshow parameter value";
	}	
	
	@Override
	public String name() {
		return "sysinfo";
	}

}
