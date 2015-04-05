package com.euromoby.rest.handler.fileinfo;

public class FileInfoResponse {
	private long length;
	private long lastModified;
	private boolean complete = true;

	public FileInfoResponse() {
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
