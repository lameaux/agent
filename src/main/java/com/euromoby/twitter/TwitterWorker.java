package com.euromoby.twitter;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.euromoby.twitter.model.TwitterAccount;
import com.euromoby.twitter.model.TwitterMessage;


public class TwitterWorker implements Callable<TwitterMessage> {

	private static final Logger log = LoggerFactory.getLogger(TwitterWorker.class);
	
	private TwitterProvider twitterProvider;
	private TwitterAccount twitterAccount;
	private TwitterMessage twitterMessage;
	
	public TwitterWorker(TwitterProvider twitterProvider, TwitterAccount twitterAccount, TwitterMessage twitterMessage) {
		this.twitterProvider = twitterProvider;
		this.twitterMessage = twitterMessage;
		this.twitterAccount = twitterAccount;
	}

	@Override
	public TwitterMessage call() throws Exception {
		log.debug("Sending message to {} ({})", twitterAccount.getScreenName(), twitterAccount.getId());
		twitterProvider.sendMessage(twitterAccount, twitterMessage.getMessageText());
		return twitterMessage;
	}	
	
}
