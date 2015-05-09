package com.euromoby.download.model;

public class DownloadFile {
	private Integer id;
	private String url;
	private String fileLocation;
	private boolean noProxy;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getFileLocation() {
		return fileLocation;
	}

	public void setFileLocation(String fileLocation) {
		this.fileLocation = fileLocation;
	}

	public boolean isNoProxy() {
		return noProxy;
	}

	public void setNoProxy(boolean noProxy) {
		this.noProxy = noProxy;
	}

}
