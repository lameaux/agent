package processor;

import model.AgentId;
import ping.PingSender;
import storage.ping.PingStatus;
import utils.StringUtils;
import agent.Agent;

public class PingCommand extends CommandBase implements Command {

	private PingSender pingSender = Agent.get().getPingSender(); 
	
	public String execute(String request) {
		String[] params = parameters(request);
		if (params.length < 2 || StringUtils.nullOrEmpty(params[0]) || StringUtils.nullOrEmpty(params[1])) {
			return syntaxError();
		}

		String hostname = params[0].toLowerCase();		
		int restPort;
		try {
			restPort = Integer.parseInt(params[1].toLowerCase());
		} catch (NumberFormatException e) {
			return syntaxError();			
		}

		AgentId targetAgent = new AgentId(hostname, null, 0);		
		
		PingStatus pingStatus = new PingStatus();
		long start = System.currentTimeMillis();
		try {
			targetAgent = pingSender.ping(hostname, restPort);
			pingStatus.setError(false);
		} catch (Exception e) {
			pingStatus.setError(true);
			pingStatus.setMessage(e.getMessage());
		} finally {
			pingStatus.setTime(System.currentTimeMillis() - start);			
		}
		
		return targetAgent.toString() + "\r\n" + pingStatus.toString();
		
	}

	@Override
	public String help() {
		return "ping hostname rest.port, Example: ping localhost 21080";
	}	
	
	@Override
	public String name() {
		return "ping";
	}

}
