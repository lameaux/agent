package com.euromoby.download.scheduler;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.Config;
import com.euromoby.download.DownloadManager;
import com.euromoby.download.client.DownloadClient;
import com.euromoby.download.model.DownloadFile;
import com.euromoby.file.FileProvider;
import com.euromoby.service.SchedulerService;

@Component
public class DownloadScheduler extends SchedulerService {

	private static final Logger LOG = LoggerFactory.getLogger(DownloadScheduler.class);

	public static final String SERVICE_NAME = "download";
	
	private Config config;	
	private DownloadManager downloadManager;
	private DownloadClient downloadClient;
	private FileProvider fileProvider;	

	private ExecutorService executor;
	private ExecutorCompletionService<DownloadFile> completionService;	

	@Autowired
	public DownloadScheduler(Config config, DownloadManager downloadManager, DownloadClient downloadClient, FileProvider fileProvider) {
		this.config = config;
		this.downloadManager = downloadManager;
		this.downloadClient = downloadClient;
		this.fileProvider = fileProvider;
		
		executor = Executors.newFixedThreadPool(this.config.getDownloadPoolSize());
		completionService = new ExecutorCompletionService<DownloadFile>(executor);		
	}

	@Override
	public void executeInternal() throws InterruptedException {
		List<DownloadFile> files = downloadManager.getScheduledFiles(config.getDownloadSchedulerBatchSize());
		for (DownloadFile file : files) {
			completionService.submit(new DownloadWorker(file, downloadClient, fileProvider));
		}		
		for (int i = 0; i < files.size(); i++) {
			Future<DownloadFile> downloadFileFuture = completionService.take();
			try {
				downloadFileFuture.get();
			} catch (ExecutionException e) {
				LOG.debug("Download error: {}", e.getMessage());
			}			
		}
		downloadManager.deleteScheduledFiles(files);
	}

	@Override	
	public int getSleepTime() {
		return config.getDownloadSchedulerInterval();
	}	
	
	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}

}
