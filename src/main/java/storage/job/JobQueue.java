package storage.job;

import java.util.PriorityQueue;

public class JobQueue {

	private PriorityQueue<Job> q = new PriorityQueue<Job>();

	public boolean hasNextJob() {
		Job job = q.peek();
		return job != null && job.canStartNow();
	}

	public Job pollJob() {
		return q.poll();
	}

	public void pushJob(Job job) {
		q.offer(job);
	}
}
