package job;

import java.util.UUID;
import java.util.concurrent.Callable;

import utils.DateUtils;

public abstract class Job implements Callable<JobDetail>, Comparable<Job> {

	protected UUID uuid;
	private long scheduleTime;

	public Job(long scheduleTime) {
		uuid = UUID.randomUUID();
		this.scheduleTime = scheduleTime;
	}

	@Override
	public int compareTo(Job o) {
		return this.scheduleTime < o.scheduleTime ? -1 : this.scheduleTime > o.scheduleTime ? 1 : 0;
	}

	public boolean canStartNow() {
		return scheduleTime < System.currentTimeMillis();
	}

	public UUID getUuid() {
		return uuid;
	}
	
	public long getScheduleTime() {
		return scheduleTime;
	}
	
	public String description() {
		return this.getClass().getSimpleName() + "(" + uuid.toString() + ") is scheduled for " + DateUtils.iso(scheduleTime);
	}

	public String name() {
		return this.getClass().getSimpleName() + "(" + uuid.toString() + ")";
	}
	
	public abstract JobDetail createJobStatus();
}
