package com.euromoby.agent.model;

public class AgentStatus {

	public static final long ACTIVE_SINCE_LAST_PING = 10 * 60 * 1000L; // 10
																		// minutes
	public static final long PING_INTERVAL = 1 * 60 * 1000L; // 1 minute

	private String myHost;
	private long lastPingSendAttempt = 0;
	private long lastPingSendSuccess = 0;
	private long lastPingReceived = 0;
	private long freeSpace;

	public String getMyHost() {
		return myHost;
	}

	public void setMyHost(String myHost) {
		this.myHost = myHost;
	}

	public long getLastPingSendAttempt() {
		return lastPingSendAttempt;
	}

	public void setLastPingSendAttempt(long lastPingSendAttempt) {
		this.lastPingSendAttempt = lastPingSendAttempt;
	}

	public long getLastPingSendSuccess() {
		return lastPingSendSuccess;
	}

	public void setLastPingSendSuccess(long lastPingSendSuccess) {
		this.lastPingSendSuccess = lastPingSendSuccess;
	}

	public long getLastPingReceived() {
		return lastPingReceived;
	}

	public void setLastPingReceived(long lastPingReceived) {
		this.lastPingReceived = lastPingReceived;
	}

	public long getFreeSpace() {
		return freeSpace;
	}

	public void setFreeSpace(long freeSpace) {
		this.freeSpace = freeSpace;
	}

	public boolean isActive() {
		return System.currentTimeMillis() - Math.max(lastPingSendSuccess, lastPingReceived) < ACTIVE_SINCE_LAST_PING;
	}

	public boolean isPingRequired() {
		return System.currentTimeMillis() - lastPingSendAttempt > PING_INTERVAL;
	}

}
