package com.euromoby.rest.handler.fileinfo;

import com.euromoby.model.AgentId;

public class FileInfo {
	private AgentId agentId;
	private String fileLocation;
	private long length;
	private long lastModified;
	private boolean complete = true;

	public FileInfo() {
	}

	public AgentId getAgentId() {
		return agentId;
	}

	public void setAgentId(AgentId agentId) {
		this.agentId = agentId;
	}

	public String getFileLocation() {
		return fileLocation;
	}

	public void setFileLocation(String fileLocation) {
		this.fileLocation = fileLocation;
	}

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}

	public long getLastModified() {
		return lastModified;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	public boolean isComplete() {
		return complete;
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
	}

}
