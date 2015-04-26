package com.euromoby.rest.handler.twitter;

import io.netty.handler.codec.http.FullHttpResponse;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import twitter4j.auth.AccessToken;

import com.euromoby.http.HttpResponseProvider;
import com.euromoby.rest.RestException;
import com.euromoby.rest.handler.RestHandlerBase;
import com.euromoby.twitter.TwitterProvider;
import com.euromoby.utils.ListUtils;
import com.euromoby.utils.StringUtils;

@Component
public class TwitterOAuthHandler extends RestHandlerBase {

	public static final String URL = "/twitter/oauth";
	public static final String OAUTH_TOKEN = "oauth_token";
	public static final String OAUTH_VERIFIER = "oauth_verifier";
	
	private TwitterProvider twitterProvider;
	
	@Autowired
	public TwitterOAuthHandler(TwitterProvider twitterProvider) {
		this.twitterProvider = twitterProvider;
	}
	
	@Override
	public boolean matchUri(URI uri) {
		return uri.getPath().equals(URL);
	}	
	
	@Override
	public FullHttpResponse doGet() {
		
		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);		
		
		Map<String, List<String>> parameters = getUriAttributes();
		String oauthToken = ListUtils.getFirst(parameters.get(OAUTH_TOKEN));
		String oauthVerifier = ListUtils.getFirst(parameters.get(OAUTH_VERIFIER));
		if (StringUtils.nullOrEmpty(oauthToken) || StringUtils.nullOrEmpty(oauthVerifier)) {
			RestException re = new RestException("Invalid " + OAUTH_TOKEN + " or " + OAUTH_VERIFIER);
			return httpResponseProvider.errorResponse(re);			
		}
		try {
			AccessToken accessToken = twitterProvider.getAccessToken(oauthToken, oauthVerifier);
			twitterProvider.storeAccessToken(accessToken);
			return httpResponseProvider.createRedirectResponse(TwitterHandler.URL);			
			
		} catch (Exception e) {
			return httpResponseProvider.errorResponse(new RestException(e));
		}

	}

}
