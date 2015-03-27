package com.euromoby.job;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class JobManager {

	private static final Logger LOG = LoggerFactory.getLogger(JobManager.class);

	private PriorityQueue<JobDetail> q = new PriorityQueue<JobDetail>();

	private Map<UUID, JobDetail> jobDetails = new HashMap<UUID, JobDetail>();
	
	public synchronized boolean hasNewJob() {
		JobDetail job = q.peek();
		return job != null && job.canStartNow();
	}

	public synchronized JobDetail getNextJob() {
		return q.poll();
	}

	public synchronized void submit(JobDetail jobDetail) {
		jobDetails.put(jobDetail.getUuid(), jobDetail);
		q.offer(jobDetail);
	}	
	
	public synchronized void notify(JobDetail jobDetail) {
		// TODO
		LOG.info(jobDetail.toString());
	}

	public synchronized Set<JobDetail> getSnapshot() {
		Set<JobDetail> set = new TreeSet<JobDetail>(Collections.reverseOrder());
		for (JobDetail jobDetail : jobDetails.values()) {
			set.add(new JobDetail(jobDetail));
		}
		return set;
	}
	
}
