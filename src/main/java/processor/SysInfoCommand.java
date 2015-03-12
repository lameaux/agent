package processor;


public class SysInfoCommand extends CommandBase implements Command {

	public String execute(String request) {

		String[] params = parameters(request);
		if (params.length == 1 && !nullOrEmpty(params[0])) {
			String param = params[0];
			if (param.equals("os")) return os();
			if (param.equals("arch")) return arch();
			if (param.equals("cpus")) return String.valueOf(cpus());			
		}

		return String.format("arch: %s, cpus: %d, os: %s", arch(), cpus(), os());
	}

	private String arch() {
		return System.getProperty("os.arch");
	}

	private String os() {
		return System.getProperty("os.name") + " v" + System.getProperty("os.version");
	}
	
	private int cpus() {
		return Runtime.getRuntime().availableProcessors();
	}
	
	@Override
	public String help() {
		return "sysinfo [arch|cpus|os]";
	}	
	
	@Override
	public String name() {
		return "sysinfo";
	}

}
