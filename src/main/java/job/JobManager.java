package job;

import java.util.PriorityQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.DateUtils;

public class JobManager {

	private static final Logger LOG = LoggerFactory.getLogger(JobManager.class);

	private PriorityQueue<Job> q = new PriorityQueue<Job>();

	public boolean hasNewJob() {
		Job job = q.peek();
		return job != null && job.canStartNow();
	}

	public Job getNextJob() {
		return q.poll();
	}

	public void submit(Job job) {
		q.offer(job);
	}	
	
	public void notify(JobStatus jobStatus) {
		StringBuffer sb = new StringBuffer();
		sb.append(jobStatus.getJobClass()).append(" | ");
		sb.append(jobStatus.getUuid()).append(" | ");
		sb.append(DateUtils.iso(jobStatus.getStartTime())).append(" | ");		
		sb.append(DateUtils.iso(jobStatus.getFinishTime())).append(" | ");
		sb.append(jobStatus.getState().toString());
		if (jobStatus.isError()) {
			sb.append(" | Error: ").append(jobStatus.getMessage());
		}
		LOG.info(sb.toString());
	}

}
