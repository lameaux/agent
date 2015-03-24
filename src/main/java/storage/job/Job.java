package storage.job;

public abstract class Job implements Runnable, Comparable<Job> {

	private long startTime;

	public Job(long startTime) {
		this.startTime = startTime;
	}

	@Override
	public int compareTo(Job o) {
		return this.startTime < o.startTime ? -1 : this.startTime > o.startTime ? 1 : 0;
	}

	public boolean canStartNow() {
		return startTime < System.currentTimeMillis();
	}

}
