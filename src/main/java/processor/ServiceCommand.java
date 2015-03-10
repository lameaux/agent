package processor;

import telnet.TelnetServer;
import agent.Agent;

public class ServiceCommand extends CommandBase implements Command {

	public String execute(String request) {

		String[] params = parameters(request);
		if (params.length < 2 || nullOrEmpty(params[0]) || nullOrEmpty(params[1])) {
			return syntaxError();
		}

		String serviceName = params[0];
		String action = params[1];

		if (serviceName.equals("telnet")) {
			if (action.equals("stop")) {
				Agent.serviceManager.stopService(TelnetServer.SERVICE_NAME);
			} else if (action.equals("status")) {
				return Agent.serviceManager.getState(TelnetServer.SERVICE_NAME).toString();
			}
			
			return "Unable to " + action + " " + serviceName;
		}

		return "Unknown service";
	}

	@Override
	public String help() {
		return "service web status|start|stop|restart";
	}

	@Override
	public String name() {
		return "service";
	}

}
