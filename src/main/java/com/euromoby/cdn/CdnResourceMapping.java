package com.euromoby.cdn;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.Config;
import com.euromoby.cdn.model.CdnResource;
import com.google.gson.Gson;

@Component
public class CdnResourceMapping {

	private static final Logger log = LoggerFactory.getLogger(CdnResourceMapping.class);
	private static final Gson gson = new Gson();
	
	private Config config;
	private CdnResource[] cdnResources = new CdnResource[0];

	@Autowired
	public CdnResourceMapping(Config config) {
		this.config = config;
		loadMappingFile();
	}

	public CdnResource findByUrl(String url) {
		for (CdnResource cdnResource : cdnResources) {
			if (cdnResource.matches(url)) {
				return cdnResource;
			}
		}
		return null;
	}

	protected void loadMappingFile() {
		File mappingFile = new File(config.getAgentCdnMappingFile());
		if (!mappingFile.exists() || !mappingFile.isFile()) {
			return;
		}
		try {
			String json = FileUtils.readFileToString(mappingFile);
			cdnResources = gson.fromJson(json, CdnResource[].class);
		} catch (Exception e) {
			log.error("Loading of CDN resource mapping failed", e);
		}
	}
}
