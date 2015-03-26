package com.euromoby.processor;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.service.ServiceManager;
import com.euromoby.service.ServiceState;
import com.euromoby.utils.StringUtils;


@Component
public class ServiceCommand extends CommandBase implements Command {

	private ServiceManager serviceManager;	

	@Autowired
	public void setServiceManager(ServiceManager serviceManager) {
		this.serviceManager = serviceManager;
	}

	@Override
	public String execute(String request) {

		String[] params = parameters(request);
		
		// get all states
		if (params.length == 0) {
			Map<String, ServiceState> allStates = serviceManager.getAllStates();
			StringBuffer sb = new StringBuffer();
			for (String serviceName : allStates.keySet()) {
				ServiceState serviceState = allStates.get(serviceName);
				sb.append(serviceName).append(": ").append(serviceState.toString()).append(StringUtils.CRLF);
			}
			return sb.toString();
		}
		
		if (params.length < 2 || StringUtils.nullOrEmpty(params[0]) || StringUtils.nullOrEmpty(params[1])) {
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
		
		return "service\t\t\t\tshow status of all services" + StringUtils.CRLF +
				"service\t<name>\tstatus\t\tshow service status" + StringUtils.CRLF +
				"service\t<name>\tstop\t\tstop service" + StringUtils.CRLF +
				"service\t<name>\trestart\t\trestart service" + StringUtils.CRLF +				
				"service\t<name>\tstart\t\tstart service";		
		
	}

	@Override
	public String name() {
		return "service";
	}

}
