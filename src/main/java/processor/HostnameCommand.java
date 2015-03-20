package processor;

import utils.NetUtils;

public class HostnameCommand extends CommandBase implements Command {

	@Override
	public String execute(String request) {
		return NetUtils.getHostname();
	}

	@Override
	public String name() {
		return "hostname";
	}

}
