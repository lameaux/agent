package com.euromoby.job;

import java.util.Map;

import com.euromoby.job.model.JobDetail;
import com.euromoby.job.model.JobState;
import com.euromoby.model.AgentId;


public class GetNewJobsJob extends Job {

	public static final String PARAM_AGENT_ID = "agentId";
	public static final String PARAM_NOPROXY = "noproxy";
	
	private GetJobsClient getJobsClient;
	
	public GetNewJobsJob(JobDetail jobDetail, GetJobsClient getJobsClient) {
		super(jobDetail);
		this.getJobsClient = getJobsClient;
	}

	@Override
	public JobDetail call() throws Exception {
		jobDetail.setState(JobState.RUNNING);
		jobDetail.setStartTime(System.currentTimeMillis());
		try {
			Map<String, String> parameters = jobDetail.getParameters();
			validate(parameters);
			String agentIdString = parameters.get(PARAM_AGENT_ID);
			boolean noProxy = Boolean.valueOf(parameters.get(PARAM_NOPROXY));	
			 
			JobDetail[] newJobs = getJobsClient.getJobs(new AgentId(agentIdString), noProxy);
			
			jobDetail.setState(JobState.FINISHED);
		} catch (Exception e) {
			jobDetail.setError(true);
			jobDetail.setMessage(e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
			jobDetail.setState(JobState.FAILED);
		} finally {
			jobDetail.setFinishTime(System.currentTimeMillis());
		}
		return jobDetail;
	}
	
	private void validate(Map<String, String> parameters) throws Exception {
		if (parameters == null || parameters.isEmpty()) {
			throw new IllegalArgumentException("Mandatory parameters are missing");
		}	
		if (!parameters.containsKey(PARAM_AGENT_ID)) {
			throw new IllegalArgumentException(PARAM_AGENT_ID + " is missing");
		}
	}
	
}
