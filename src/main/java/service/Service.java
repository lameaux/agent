package service;

public interface Service {

	void startService();
	void stopService();
	String getServiceName();
	ServiceState getServiceState();
	
}
