package com.euromoby.cdn.model;

import com.euromoby.utils.StringUtils;

public class CdnResource {

	/**
	 *  Path prefix should start with / (slash)
	 */
	private String urlPathPrefix = "";
	/**
	 * Regexp pattern (file should have an extension)
	 */
	private String urlPathPattern = "/.*";
	
	/**
	 * Url where the location can be found
	 */
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

	public boolean matches(String urlPath) {
		if (StringUtils.nullOrEmpty(urlPath)) {
			return false;
		}
		if (!StringUtils.nullOrEmpty(urlPathPrefix)) {
			if (!urlPath.startsWith(urlPathPrefix)) {
				return false;
			}
			urlPath = urlPath.substring(urlPathPrefix.length());
		}
		return urlPath.matches(urlPathPattern);
	}

	public String getSourceUrl(String url) {
		if (StringUtils.nullOrEmpty(url)) {
			return null;
		}
		if (StringUtils.nullOrEmpty(resourceOrigin)) {
			return null;
		}
		
		String resourceName = url;
		if (!StringUtils.nullOrEmpty(urlPathPrefix)) {
			resourceName = resourceName.substring(urlPathPrefix.length());
		}
		
		return resourceOrigin + resourceName;
	}

	// Getters & Setters

	public String getUrlPathPrefix() {
		return urlPathPrefix;
	}

	public void setUrlPathPrefix(String urlPathPrefix) {
		this.urlPathPrefix = urlPathPrefix;
	}

	public String getUrlPathPattern() {
		return urlPathPattern;
	}

	public void setUrlPathPattern(String urlPathPattern) {
		this.urlPathPattern = urlPathPattern;
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
