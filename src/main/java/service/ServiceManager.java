package service;

import java.util.HashMap;
import java.util.Map;

public class ServiceManager {

	private Map<String, Service> services = new HashMap<String, Service>();

	public synchronized void registerService(Service service) {
		services.put(service.getServiceName(), service);
	}

	public synchronized void startService(String serviceName) {
		Service service = services.get(serviceName);
		if (service != null && service.getServiceState() != ServiceState.RUNNING) {
			service.startService();
		}
	}

	public synchronized void stopService(String serviceName) {
		Service service = services.get(serviceName);
		if (service != null && service.getServiceState() != ServiceState.STOPPED) {
			service.stopService();
		}
	}

	public synchronized ServiceState getState (String serviceName) {
		Service service = services.get(serviceName);
		if (service == null) {
			return ServiceState.UNKNOWN;
		}
		return service.getServiceState();
	}	
	
	public synchronized void shutdownAll() {
		for (Service service : services.values()) {
			if (service.getServiceState() == ServiceState.RUNNING) {
				service.stopService();
			}
		}
	}

}
