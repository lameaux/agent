package com.euromoby.ping.model;

public class PingStatus {

	private static final long ACTIVE_TIME_LIMIT = 10 * 60 * 1000; // 10 minutes

	private boolean error = false;
	private long time = 0;
	private String message;

	public PingStatus() {
		time = System.currentTimeMillis();
	}

	public PingStatus(boolean error) {
		this();
		this.error = error;
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isActive() {
		if (error) {
			return false;
		}

		return System.currentTimeMillis() - time < ACTIVE_TIME_LIMIT;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (error ? 1231 : 1237);
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + (int) (time ^ (time >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PingStatus other = (PingStatus) obj;
		if (error != other.error)
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (time != other.time)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PingStatus [error=" + error + ", time=" + time + ", message=" + message + "]";
	}

}
