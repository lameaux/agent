package job;

import java.util.Map;
import java.util.UUID;

public class JobStatus {

	private String jobClass;
	private UUID uuid;
	private Map<String, String> parameters;

	private JobState state = JobState.NEW;
	private long startTime = 0;
	private long finishTime = 0;
	private boolean error = false;
	private String message;

	public JobStatus(String jobClass, UUID uuid) {
		this.jobClass = jobClass;
		this.uuid = uuid;
	}

	public UUID getUuid() {
		return uuid;
	}

	public String getJobClass() {
		return jobClass;
	}

	public JobState getState() {
		return state;
	}

	public void setState(JobState state) {
		this.state = state;
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

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

}
