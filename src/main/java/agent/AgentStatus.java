package agent;

public class AgentStatus {

	private static final long ACTIVE_LAST_PING = 60 * 60 * 1000L; // 1 hour
	
	private String myHost;
	private long lastPing = 0;

	public String getMyHost() {
		return myHost;
	}

	public void setMyHost(String myHost) {
		this.myHost = myHost;
	}

	public long getLastPing() {
		return lastPing;
	}

	public void setLastPing(long lastPing) {
		this.lastPing = lastPing;
	}

	public boolean isActive() {
		return System.currentTimeMillis() - lastPing < ACTIVE_LAST_PING;
	}
	
}
