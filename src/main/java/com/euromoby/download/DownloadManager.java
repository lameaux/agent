package com.euromoby.download;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.euromoby.download.dao.DownloadFileDao;
import com.euromoby.model.AgentId;

@Component
public class DownloadManager {

	private DownloadFileDao downloadFileDao;
	
	@Autowired
	public DownloadManager(DownloadFileDao downloadFileDao) {
		this.downloadFileDao = downloadFileDao;
	}

	@Transactional	
	public DownloadFile scheduleDownloadFile(String url, String fileLocation, boolean noProxy) {
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
		// TODO: send download request
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
