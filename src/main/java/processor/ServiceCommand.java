package processor;

import service.ServiceManager;
import service.ServiceState;

public class ServiceCommand extends CommandBase implements Command {

	public ServiceCommand(ServiceManager serviceManager) {
		this.serviceManager = serviceManager;
	}

	private final ServiceManager serviceManager;

	@Override
	public String execute(String request) {

		String[] params = parameters(request);
		if (params.length < 2 || nullOrEmpty(params[0]) || nullOrEmpty(params[1])) {
			return syntaxError();
		}

		String serviceName = params[0].toLowerCase();
		String action = params[1].toLowerCase();

		if (!serviceManager.isAvailable(serviceName)) {
			return "Service " + serviceName + " is not available";
		}

		if (!serviceManager.isAllowedAction(serviceName, action)) {
			return "Unable to " + action + " " + serviceName;
		}

		ServiceState status = serviceManager.executeAction(serviceName, action);

		return serviceName + ": " + status.toString();

	}

	@Override
	public String help() {
		return "service rest status|start|stop|restart";
	}

	@Override
	public String name() {
		return "service";
	}

}
