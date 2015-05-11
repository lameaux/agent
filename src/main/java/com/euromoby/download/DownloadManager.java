package com.euromoby.download;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.euromoby.agent.Config;
import com.euromoby.download.client.DownloadRequestSender;
import com.euromoby.download.dao.DownloadFileDao;
import com.euromoby.download.model.DownloadFile;
import com.euromoby.model.AgentId;
import com.euromoby.utils.SystemUtils;

@Component
public class DownloadManager {

	private static final Logger log = LoggerFactory.getLogger(DownloadManager.class);
	
	private Config config;
	private DownloadFileDao downloadFileDao;
	private DownloadRequestSender downloadRequestSender;
	
	@Autowired
	public DownloadManager(Config config, DownloadFileDao downloadFileDao, DownloadRequestSender downloadRequestSender) {
		this.config = config;
		this.downloadFileDao = downloadFileDao;
		this.downloadRequestSender = downloadRequestSender;
	}

	@Transactional	
	public DownloadFile scheduleDownloadFile(String url, String fileLocation, boolean noProxy) {
		
		long freeSpace = SystemUtils.getFreeSpace(config.getAgentFilesPath());
		if (freeSpace < config.getDownloadFreespaceMin()) {
			log.warn("Not enough free space for files: {}", freeSpace);
			return null;
		}
		
		DownloadFile downloadFile = downloadFileDao.findByUrl(url);
		if (downloadFile == null) {
			downloadFile = new DownloadFile();
			downloadFile.setUrl(url);
			downloadFile.setFileLocation(fileLocation);
			downloadFile.setNoProxy(noProxy);
			downloadFileDao.save(downloadFile);
		}
		return downloadFile;
	}

	public void askAgentToDownloadFile(AgentId agentId, String url, String fileLocation) {
		try {
			downloadRequestSender.sendDownloadRequest(agentId, url, fileLocation);
		} catch (Exception e) {
			log.warn("Unable to send download request to {}", agentId);
		}
	}
	
	@Transactional(readOnly=true)
	public DownloadFile findScheduledFileLocation(String fileLocation) {
		return downloadFileDao.findByFileLocation(fileLocation);
	}
	
	@Transactional(readOnly=true)
	public List<DownloadFile> getScheduledFiles(int limit) {
		return downloadFileDao.findAll(limit);
	}
	
	@Transactional
	public void deleteScheduledFiles(List<DownloadFile> downloadMessages) {
		downloadFileDao.deleteAll(downloadMessages);
	}	
	
}
