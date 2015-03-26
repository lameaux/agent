package com.euromoby.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.DisposableBean;

public class ServiceManager implements DisposableBean {

	public static final String ACTION_STATUS = "status";
	public static final String ACTION_START = "start";
	public static final String ACTION_STOP = "stop";
	public static final String ACTION_RESTART = "restart";

	private static final List<String> allowedActions = Arrays.asList(ACTION_STATUS, ACTION_START, ACTION_STOP,
			ACTION_RESTART);

	private final Map<String, Service> services = new ConcurrentHashMap<String, Service>();

	public void registerService(Service service) {
		services.put(service.getServiceName(), service);
	}

	public ServiceState executeAction(final String serviceName, final String action) {

		Service service = services.get(serviceName);
		if (service == null) {
			return ServiceState.UNKNOWN;
		}

		if (ACTION_START.equals(action)) {
			if (service.getServiceState() != ServiceState.RUNNING) {
				service.startService();
			}
			return service.getServiceState();
		}
		if (ACTION_STOP.equals(action)) {
			service.stopService();
			return service.getServiceState();
		}
		if (ACTION_RESTART.equals(action)) {
			service.stopService();
			service.startService();
			return service.getServiceState();
		}
		if (ACTION_STATUS.equals(action)) {
			return service.getServiceState();
		}
		return ServiceState.UNKNOWN;

	}

	public synchronized ServiceState getState(String serviceName) {
		Service service = services.get(serviceName);
		if (service == null) {
			return ServiceState.UNKNOWN;
		}
		return service.getServiceState();
	}

	public synchronized boolean isAvailable(String serviceName) {
		return services.containsKey(serviceName);
	}

	public synchronized boolean isAllowedAction(String serviceName, String action) {
		return allowedActions.contains(action);
	}

	public synchronized void shutdownAll() {
		for (Service service : services.values()) {
			if (service.getServiceState() == ServiceState.RUNNING) {
				service.stopService();
			}
		}
	}

	public synchronized Map<String, ServiceState> getAllStates() {
		Map<String, ServiceState> map = new HashMap<String, ServiceState>();
		for (String serviceName : services.keySet()) {
			Service service = services.get(serviceName);
			map.put(serviceName, service.getServiceState());
		}
		return map;
	}

	@Override
	public void destroy() throws Exception {
		shutdownAll();
	}
	
}
