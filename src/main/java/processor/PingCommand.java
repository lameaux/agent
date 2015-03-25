package processor;

import model.AgentId;
import ping.PingSender;
import storage.ping.PingStatus;
import utils.StringUtils;
import agent.Agent;

public class PingCommand extends CommandBase implements Command {

	private static final String NO_PROXY = "noproxy"; 
	
	private PingSender pingSender = Agent.get().getPingSender();
	
	@Override
	public String execute(String request) {
		String[] params = parameters(request);
		if (params.length < 1 || StringUtils.nullOrEmpty(params[0])) {
			return syntaxError();
		}

		String url = params[0];
		boolean noProxy = (params.length == 2 && NO_PROXY.equals(params[1]));

		AgentId targetAgent = null;		
		
		PingStatus pingStatus = new PingStatus();
		long start = System.currentTimeMillis();
		try {
			targetAgent = pingSender.ping(url, noProxy);
			return targetAgent.toString() + "\r\n" + pingStatus.toString() + "\r\nResponse time:" + (pingStatus.getTime() - start);
		} catch (Exception e) {
			pingStatus.setError(true);
			pingStatus.setMessage(e.getMessage());
		} finally {
			pingStatus.setTime(System.currentTimeMillis());
		}
		
		return url + "\r\n" + pingStatus.toString() + "\r\nResponse time:" + (pingStatus.getTime() - start);
		
	}

	@Override
	public String help() {
		return "ping url [noproxy], Example: ping http://localhost:21080/ping noproxy";
	}	
	
	@Override
	public String name() {
		return "ping";
	}

}
