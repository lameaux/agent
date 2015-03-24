package job;

import java.util.PriorityQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobManager {

	private static final Logger LOG = LoggerFactory.getLogger(JobManager.class);

	private PriorityQueue<JobDetail> q = new PriorityQueue<JobDetail>();

	public boolean hasNewJob() {
		JobDetail job = q.peek();
		return job != null && job.canStartNow();
	}

	public JobDetail getNextJob() {
		return q.poll();
	}

	public void submit(JobDetail job) {
		q.offer(job);
	}	
	
	public void notify(JobDetail jobDetail) {
		LOG.info(jobDetail.toString());
	}

}
