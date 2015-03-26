package processor;

import java.util.Arrays;

import utils.NetUtils;
import utils.StringUtils;

public class InetAddressCommand extends CommandBase implements Command {

	@Override
	public String execute(String request) {
		StringBuffer sb = new StringBuffer();
		sb.append("Hostname: ").append(NetUtils.getHostname()).append(StringUtils.CRLF);
		sb.append("Addresses: ").append(Arrays.toString(NetUtils.getAllInterfaces().toArray()));
		return sb.toString();
	}

	@Override
	public String name() {
		return "inetaddress";
	}

}
