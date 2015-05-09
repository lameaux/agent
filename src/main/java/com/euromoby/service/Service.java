package com.euromoby.service;

import com.euromoby.service.model.ServiceState;

public interface Service extends Runnable {

	void startService();

	void stopService();

	String getServiceName();

	ServiceState getServiceState();

}
