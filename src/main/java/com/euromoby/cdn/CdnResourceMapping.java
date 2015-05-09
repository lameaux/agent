package com.euromoby.cdn;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.euromoby.cdn.model.CdnResource;

@Component
public class CdnResourceMapping {
	
	private List<CdnResource> cdnResources = new ArrayList<CdnResource>();
	
	public CdnResourceMapping() {
		// default - matches all
		cdnResources.add(new CdnResource());		
	}
	
	public CdnResource findByUrl(String url) {
		for (CdnResource cdnResource : cdnResources) {
			if (cdnResource.matches(url)) {
				return cdnResource;
			}
		}
		return null;
	}

	public void setCdnResources(List<CdnResource> cdnResources) {
		this.cdnResources = cdnResources;
	}
	
}
