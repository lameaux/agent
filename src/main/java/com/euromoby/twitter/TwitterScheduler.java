package com.euromoby.twitter;

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
import com.euromoby.service.SchedulerService;
import com.euromoby.twitter.model.TwitterAccount;
import com.euromoby.twitter.model.TwitterMessage;

@Component
public class TwitterScheduler extends SchedulerService {

	private static final Logger LOG = LoggerFactory.getLogger(TwitterScheduler.class);

	public static final String SERVICE_NAME = "twitter";
	
	private Config config;	
	private TwitterManager twitterManager;
	private TwitterProvider twitterProvider;

	private ExecutorService executor;
	private ExecutorCompletionService<TwitterMessage> completionService;	

	@Autowired
	public TwitterScheduler(Config config, TwitterManager twitterManager, TwitterProvider twitterProvider) {
		this.config = config;
		this.twitterManager = twitterManager;
		this.twitterProvider = twitterProvider;
		
		executor = Executors.newFixedThreadPool(this.config.getTwitterPoolSize());
		completionService = new ExecutorCompletionService<TwitterMessage>(executor);		
	}

	@Override
	public void executeInternal() throws InterruptedException {
		List<TwitterMessage> messages = twitterManager.getScheduledMessages(config.getTwitterSchedulerBatchSize());
		for (TwitterMessage message : messages) {
			TwitterAccount account = twitterManager.getAccountById(message.getAccountId());
			completionService.submit(new TwitterWorker(twitterProvider, account, message));
		}		
		for (int i = 0; i < messages.size(); i++) {
			Future<TwitterMessage> twitterMessageFuture = completionService.take();
			try {
				twitterMessageFuture.get();
			} catch (ExecutionException e) {
				LOG.debug("Twitter error: {}", e.getMessage());
			}			
		}
		twitterManager.deleteScheduledMessages(messages);
	}

	@Override	
	public int getSleepTime() {
		return config.getTwitterSchedulerInterval();
	}	
	
	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}

}
