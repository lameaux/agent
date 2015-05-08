package com.euromoby.twitter;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import twitter4j.auth.AccessToken;

import com.euromoby.dao.TwitterAccountDao;

@Component
public class TwitterManager {

	private TwitterAccountDao twitterAccountDao;

	@Autowired
	public TwitterManager(TwitterAccountDao twitterAccountDao) {
		this.twitterAccountDao = twitterAccountDao;
	}

	@Transactional(readOnly=true)	
	public List<TwitterAccount> getAccounts() {
		return twitterAccountDao.findAll();
	}	
	
	@Transactional(readOnly=true)
	public TwitterAccount getAccountById(String id) {
		return twitterAccountDao.findById(id);
	}	
	
	@Transactional
	public void updateAccount(TwitterAccount twitterAccount) {
		twitterAccountDao.update(twitterAccount);
	}

	@Transactional	
	public void saveAccessToken(AccessToken accessToken) {
		String userId = String.valueOf(accessToken.getUserId());
		TwitterAccount account = twitterAccountDao.findById(userId);
		if (account == null) {
			account = new TwitterAccount();
			account.setId(userId);
			account.setScreenName(accessToken.getScreenName());
			account.setTags("");
			account.setAccessToken(accessToken.getToken());
			account.setAccessTokenSecret(accessToken.getTokenSecret());
			twitterAccountDao.save(account);
		} else {
			account.setScreenName(accessToken.getScreenName());
			account.setAccessToken(accessToken.getToken());
			account.setAccessTokenSecret(accessToken.getTokenSecret());
			twitterAccountDao.update(account);
		}
	}

}
