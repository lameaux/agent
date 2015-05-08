package com.euromoby.rest.handler.twitter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.http.HttpResponseProvider;
import com.euromoby.rest.RestException;
import com.euromoby.rest.handler.RestHandlerBase;
import com.euromoby.twitter.TwitterProvider;

@Component
public class TwitterConnectHandler extends RestHandlerBase {

	public static final String URL = "/twitter/connect";

	private TwitterProvider twitterProvider;

	@Autowired
	public TwitterConnectHandler(TwitterProvider twitterProvider) {
		this.twitterProvider = twitterProvider;

	}

	@Override
	public boolean matchUri(URI uri) {
		return uri.getPath().equals(URL);
	}

	@Override
	public FullHttpResponse doGet(ChannelHandlerContext ctx, HttpRequest request, Map<String, List<String>> queryParameters) throws Exception {
		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
		try {
			String authorizationUrl = twitterProvider.getAuthorizationUrl();
			return httpResponseProvider.createRedirectResponse(authorizationUrl);
		} catch (Exception e) {
			RestException re = new RestException(HttpResponseStatus.INTERNAL_SERVER_ERROR, e);
			return httpResponseProvider.errorResponse(re);
		}
	}

}
