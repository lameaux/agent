package com.euromoby.twitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

import com.euromoby.agent.Config;
import com.euromoby.utils.StringUtils;

@Component
public class TwitterProvider {

	private Config config;
	
	private Map<String, String> requestTokens = new ConcurrentHashMap<String, String>();
	private Map<String, AccessToken> accessTokens = new ConcurrentHashMap<String, AccessToken>();

	@Autowired
	public TwitterProvider(Config config) {
		this.config = config;
	}

	protected Twitter getTwitter() {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		//cb.setDebugEnabled(true);
		cb.setOAuthConsumerKey(config.getTwitterConsumerKey());
		cb.setOAuthConsumerSecret(config.getTwitterConsumerSecret());

		if (config.isHttpProxy()) {
			cb.setHttpProxyHost(config.getHttpProxyHost());
			cb.setHttpProxyPort(config.getHttpProxyPort());
		}
		
		TwitterFactory tf = new TwitterFactory(cb.build());
		Twitter twitter = tf.getInstance();
		return twitter;
	}

	public String getAuthorizationUrl() throws Exception {
		RequestToken requestToken = getTwitter().getOAuthRequestToken();
		requestTokens.put(requestToken.getToken(), requestToken.getTokenSecret());
		return requestToken.getAuthorizationURL();
	}

	public AccessToken getAccessToken(String oauthToken, String oauthVerifier) throws Exception {
		String oauthTokenSecret = requestTokens.get(oauthToken);
		if (StringUtils.nullOrEmpty(oauthTokenSecret)) {
			throw new Exception("Token is invalid");
		}
		RequestToken requestToken = new RequestToken(oauthToken, oauthTokenSecret);
		AccessToken accessToken = getTwitter().getOAuthAccessToken(requestToken, oauthVerifier);
		return accessToken;
	}
	
	public void storeAccessToken(AccessToken accessToken) {
		accessTokens.put(accessToken.getScreenName(), accessToken);
	}
	
	public AccessToken findAccessToken(String screenName) {
		return accessTokens.get(screenName);
	}
	
	public List<String> getScreenNames() {
		List<String> screenNames = new ArrayList<String>();
		for (String screenName : accessTokens.keySet()) {
			screenNames.add(screenName);
		}
		return screenNames;
	}
	
	public Status sendMessage(String screenName, String text) throws Exception {
		AccessToken accessToken = accessTokens.get(screenName);
		if (accessToken == null) {
			throw new Exception(screenName + " is not authorized");
		}
		Twitter twitter = getTwitter();
		twitter.setOAuthAccessToken(accessToken);
		
		StatusUpdate statusUpdate = new StatusUpdate(text);
		return twitter.updateStatus(statusUpdate);		
	}
}
