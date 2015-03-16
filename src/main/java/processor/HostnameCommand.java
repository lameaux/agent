package processor;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class HostnameCommand extends CommandBase implements Command {

	@Override
	public String execute(String request) {
		return executeInternal();
	}

	private String executeInternal() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			return e.getMessage();
		}
	}

	@Override
	public String name() {
		return "hostname";
	}

}
