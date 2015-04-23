package com.euromoby.job;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.euromoby.model.AgentId;
import com.euromoby.utils.DateUtils;


public class JobDetail implements Comparable<JobDetail> {

	private AgentId sender;
	private AgentId recipient;
	
	private UUID uuid = UUID.randomUUID();	
	private String jobClass;
	private long scheduleTime = System.currentTimeMillis();
	
	private Map<String, String> parameters = null;

	private JobState state = JobState.NEW;
	private long startTime = 0;
	private long finishTime = 0;
	private boolean error = false;
	private String message;


	public JobDetail() {
	}

	public JobDetail(Class<? extends Job> jobClass) {
		this.jobClass = jobClass.getCanonicalName();
	}	
	
	public JobDetail(Class<? extends Job> jobClass, Map<String, String> parameters) {
		this.jobClass = jobClass.getCanonicalName();
		this.parameters = parameters;
	}

	public JobDetail(JobDetail that) {
		this.sender = that.sender;
		this.recipient = that.recipient;
		this.uuid = that.uuid;
		this.jobClass = that.jobClass;
		this.scheduleTime = that.scheduleTime;
		this.parameters = that.parameters;
		this.state = that.state;
		this.startTime = that.startTime;
		this.finishTime = that.finishTime;
		this.error = that.error;
		this.message = that.message;
	}
	
	
	public void merge(JobDetail that) {
		this.state = that.state;
		this.startTime = that.startTime;
		this.finishTime = that.finishTime;
		this.error = that.error;
		this.message = that.message;
	}
	
	public boolean canStartNow() {
		return scheduleTime < System.currentTimeMillis();
	}

	public String getJobClass() {
		return jobClass;
	}

	public void setJobClass(String jobClass) {
		this.jobClass = jobClass;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public long getScheduleTime() {
		return scheduleTime;
	}

	public void setScheduleTime(long scheduleTime) {
		this.scheduleTime = scheduleTime;
	}

	public JobState getState() {
		return state;
	}

	public void setState(JobState state) {
		this.state = state;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(long finishTime) {
		this.finishTime = finishTime;
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public int compareTo(JobDetail o) {
		return this.scheduleTime < o.scheduleTime ? -1 : this.scheduleTime > o.scheduleTime ? 1 : 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
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
		JobDetail other = (JobDetail) obj;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(jobClass).append("(").append(uuid).append(") was scheduled for ").append(DateUtils.iso(scheduleTime)).append(". ");
		sb.append("State: ").append(state).append(". ");
		if (state == JobState.RUNNING || state == JobState.FINISHED || state == JobState.FAILED) {
			sb.append("Started at ").append(DateUtils.iso(startTime)).append(". ");
		}
		if (state == JobState.FINISHED || state == JobState.FAILED) {
			sb.append("Finished at ").append(DateUtils.iso(finishTime)).append(". ");
			sb.append("Runtime: ").append(TimeUnit.MILLISECONDS.toSeconds(finishTime-startTime)).append(" sec. ");
		}
		if (error) {
			sb.append("Error: ").append(message).append(". ");
		}
		if (parameters != null && !parameters.isEmpty()) {
			sb.append("Parameters: ");
			for (Map.Entry<String, String> entry : parameters.entrySet()) {
				sb.append(entry.getKey()).append("=").append(entry.getValue()).append(" ");
			}
		}

		return sb.toString();
	}

	public AgentId getSender() {
		return sender;
	}

	public void setSender(AgentId sender) {
		this.sender = sender;
	}

	public AgentId getRecipient() {
		return recipient;
	}

	public void setRecipient(AgentId recipient) {
		this.recipient = recipient;
	}

}
