package com.euromoby.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SchedulerService implements Service {

	public static final int DEFAULT_SLEEP_TIME = 1000;

	private static final Logger LOG = LoggerFactory.getLogger(SchedulerService.class);

	private volatile ServiceState serviceState = ServiceState.STOPPED;
	private Object startLock = new Object();
	private volatile Thread thread = null;
	private volatile boolean interrupted = true;

	public boolean isInterrupted() {
		return interrupted;
	}

	public abstract void executeInternal() throws InterruptedException;


	@Override
	public void run() {
		synchronized (startLock) {
			interrupted = false;
			serviceState = ServiceState.RUNNING;
			startLock.notifyAll();
		}
		LOG.info("{}: started", getServiceName());

		while (!interrupted) {
			try {
				executeInternal();
				Thread.sleep(getSleepTime());
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
				interrupted = true;
				break;
			}
		}
		serviceState = ServiceState.STOPPED;
		LOG.info("{}: stopped", getServiceName());
	}

	public int getSleepTime() {
		return DEFAULT_SLEEP_TIME;
	}
	
	@Override
	public void startService() {
		if (serviceState == ServiceState.RUNNING) {
			return;
		}
		synchronized (startLock) {
			thread = new Thread(this);
			thread.start();
			try {
				startLock.wait();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	@Override
	public void stopService() {
		interrupted = true;
		try {
			if (thread != null) {
				thread.join();
				thread = null;
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}


	@Override
	public ServiceState getServiceState() {
		return serviceState;
	}

}
