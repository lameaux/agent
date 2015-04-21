package com.euromoby.cdn;

import com.euromoby.utils.StringUtils;

public class CdnResourceMapping {

	private String urlPrefix;
	/**
	 * Regexp pattern
	 */
	private String urlPattern = ".*";
	private String resourceOrigin;
	/**
	 * stream or redirect?
	 */
	private boolean streamable = false;
	/**
	 * should we download missing item?
	 */
	private boolean downloadIfMissing = false;

	public CdnResourceMapping() {
	}

	public boolean matches(String url) {
		if (StringUtils.nullOrEmpty(url)) {
			return false;
		}
		if (!StringUtils.nullOrEmpty(urlPrefix) && !url.startsWith(urlPrefix)) {
			return false;
		}
		return url.matches(urlPattern);
	}

	public String getSourceUrl(String url) {
		if (StringUtils.nullOrEmpty(url)) {
			return null;
		}
		if (StringUtils.nullOrEmpty(resourceOrigin)) {
			return null;
		}
		
		String resourceName = url;
		if (!StringUtils.nullOrEmpty(urlPrefix)) {
			resourceName = resourceName.substring(urlPrefix.length());
		}
		
		return resourceOrigin + resourceName;
	}

	// Getters & Setters

	public String getUrlPrefix() {
		return urlPrefix;
	}

	public void setUrlPrefix(String urlPrefix) {
		this.urlPrefix = urlPrefix;
	}

	public String getUrlPattern() {
		return urlPattern;
	}

	public void setUrlPattern(String urlPattern) {
		this.urlPattern = urlPattern;
	}

	public String getResourceOrigin() {
		return resourceOrigin;
	}

	public void setResourceOrigin(String resourceOrigin) {
		this.resourceOrigin = resourceOrigin;
	}

	public boolean isStreamable() {
		return streamable;
	}

	public void setStreamable(boolean streamable) {
		this.streamable = streamable;
	}

	public boolean isDownloadIfMissing() {
		return downloadIfMissing;
	}

	public void setDownloadIfMissing(boolean downloadIfMissing) {
		this.downloadIfMissing = downloadIfMissing;
	}

}
