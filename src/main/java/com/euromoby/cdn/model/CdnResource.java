package com.euromoby.cdn.model;

import com.euromoby.utils.StringUtils;

public class CdnResource {

	// should start with / (slash)
	private String urlPrefix = "";
	/**
	 * Regexp pattern
	 */
	private String urlPattern = "/.+";
	private String resourceOrigin;
	
	/**
	 * proxy or redirect?
	 */
	private boolean proxyable = false;
	
	/**
	 * Could be available in network?
	 */
	private boolean availableInNetwork = true;
	
	/**
	 * should we download missing item?
	 */
	private boolean downloadIfMissing = false;

	public CdnResource() {
	}

	public boolean matches(String url) {
		if (StringUtils.nullOrEmpty(url)) {
			return false;
		}
		if (!StringUtils.nullOrEmpty(urlPrefix)) {
			if (!url.startsWith(urlPrefix)) {
				return false;
			}
			url = url.substring(urlPrefix.length());
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

	public boolean isProxyable() {
		return proxyable;
	}

	public void setProxyable(boolean proxyable) {
		this.proxyable = proxyable;
	}

	public boolean isDownloadIfMissing() {
		return downloadIfMissing;
	}

	public void setDownloadIfMissing(boolean downloadIfMissing) {
		this.downloadIfMissing = downloadIfMissing;
	}

	public boolean isAvailableInNetwork() {
		return availableInNetwork;
	}

	public void setAvailableInNetwork(boolean availableInNetwork) {
		this.availableInNetwork = availableInNetwork;
	}
	
}
