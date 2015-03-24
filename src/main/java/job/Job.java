package job;

import java.util.UUID;
import java.util.concurrent.Callable;

import utils.DateUtils;

public abstract class Job implements Callable<JobStatus>, Comparable<Job> {

	protected UUID uuid;
	private long startTime;

	public Job(long startTime) {
		uuid = UUID.randomUUID();
		this.startTime = startTime;
	}

	@Override
	public int compareTo(Job o) {
		return this.startTime < o.startTime ? -1 : this.startTime > o.startTime ? 1 : 0;
	}

	public boolean canStartNow() {
		return startTime < System.currentTimeMillis();
	}

	public UUID getUuid() {
		return uuid;
	}
	
	public long getStartTime() {
		return startTime;
	}
	
	public String description() {
		return this.getClass().getSimpleName() + "(" + uuid.toString() + ") is scheduled for " + DateUtils.iso(startTime);
	}

	public String name() {
		return this.getClass().getSimpleName() + "(" + uuid.toString() + ")";
	}
	
	public abstract JobStatus createJobStatus();
}
