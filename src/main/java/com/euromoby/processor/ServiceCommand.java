package com.euromoby.processor;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.service.ServiceManager;
import com.euromoby.service.model.ServiceState;
import com.euromoby.utils.StringUtils;


@Component
public class ServiceCommand extends CommandBase implements Command {

	public static final String NAME = "service";
	public static final String UNKNOWN_SERVICE = "Service %s is not available";
	public static final String UNKNOWN_ACTION = "Unable to %s %s";
	public static final String SERVICE_STATUS = "%s: %s";
	
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
			return String.format(UNKNOWN_SERVICE, serviceName);
		}

		if (!serviceManager.isAllowedAction(serviceName, action)) {
			return String.format(UNKNOWN_ACTION, action, serviceName);
		}

		ServiceState status = serviceManager.executeAction(serviceName, action);

		return String.format(SERVICE_STATUS, serviceName, status.toString());

	}

	@Override
	public String help() {
		
		return NAME + "\t\t\t\tshow status of all services" + StringUtils.CRLF +
				NAME + "\t<name>\t" + ServiceManager.ACTION_STATUS + "\t\tshow service status" + StringUtils.CRLF +
				NAME + "\t<name>\t" + ServiceManager.ACTION_STOP + "\t\tstop service" + StringUtils.CRLF +
				NAME + "\t<name>\t" + ServiceManager.ACTION_RESTART + "\t\trestart service" + StringUtils.CRLF +				
				NAME + "\t<name>\t" + ServiceManager.ACTION_START + "\t\tstart service";		
	}

	@Override
	public String name() {
		return NAME;
	}

}
