package processor;

import utils.StringUtils;


public class SysInfoCommand extends CommandBase implements Command {

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
		return "sysinfo [os.arch|os.name|os.version|...]";
	}	
	
	@Override
	public String name() {
		return "sysinfo";
	}

}
