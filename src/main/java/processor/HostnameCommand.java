package processor;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class HostnameCommand extends CommandBase implements Command {

	public String execute(String request) {
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
