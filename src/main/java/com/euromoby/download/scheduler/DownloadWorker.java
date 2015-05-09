package com.euromoby.download.scheduler;

import java.io.File;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.euromoby.download.client.DownloadClient;
import com.euromoby.download.model.DownloadFile;
import com.euromoby.file.FileProvider;


public class DownloadWorker implements Callable<DownloadFile> {

	private static final Logger log = LoggerFactory.getLogger(DownloadWorker.class);
	
	private DownloadFile downloadFile;
	private DownloadClient downloadClient;
	private FileProvider fileProvider;
	
	public DownloadWorker(DownloadFile downloadFile, DownloadClient downloadClient, FileProvider fileProvider) {
		this.downloadFile = downloadFile;
		this.downloadClient = downloadClient;
		this.fileProvider = fileProvider;
	}

	@Override
	public DownloadFile call() throws Exception {
		log.debug("Downloading {} to {}", downloadFile.getUrl(), downloadFile.getFileLocation());
		File targetFile = fileProvider.getTargetFile(downloadFile.getFileLocation());
		downloadClient.download(downloadFile.getUrl(), targetFile, false);
		return downloadFile;
	}	
	
}
