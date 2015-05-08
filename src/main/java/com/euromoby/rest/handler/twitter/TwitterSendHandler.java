package com.euromoby.rest.handler.twitter;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.http.HttpResponseProvider;
import com.euromoby.http.HttpUtils;
import com.euromoby.rest.RestException;
import com.euromoby.rest.handler.RestHandlerBase;
import com.euromoby.twitter.TwitterAccount;
import com.euromoby.twitter.TwitterManager;
import com.euromoby.utils.IOUtils;
import com.euromoby.utils.ListUtils;
import com.euromoby.utils.StringUtils;
import com.google.gson.Gson;

@Component
public class TwitterSendHandler extends RestHandlerBase {
	
	public static final String URL = "/twitter/send";
	private static final String REQUEST_INPUT_ACCOUNTS = "accounts";
	private static final String REQUEST_INPUT_TEXT = "text";
	private static final int TWITTER_MAX_TEXT = 140;
	
	private static final Gson gson = new Gson();
	
	private TwitterManager twitterManager;

	
	@Autowired
	public TwitterSendHandler(TwitterManager twitterManager) {
		this.twitterManager = twitterManager;
	}	
	
	@Override
	public boolean matchUri(URI uri) {
		return uri.getPath().equals(URL);
	}	

	protected String getAccountsArray() {
		TwitterAccount[] accounts = twitterManager.getAccounts().toArray(new TwitterAccount[]{});
		return gson.toJson(accounts);
	}	

	@Override
	public FullHttpResponse doGet(ChannelHandlerContext ctx, HttpRequest request, Map<String, List<String>> queryParameters) {
		InputStream is = TwitterSendHandler.class.getResourceAsStream("twittersend.html");
		String pageContent = IOUtils.streamToString(is);
		pageContent = pageContent.replace("%ACCOUNTS_ARRAY%", getAccountsArray());
		ByteBuf content = Unpooled.copiedBuffer(pageContent, CharsetUtil.UTF_8);
		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
		return httpResponseProvider.createHttpResponse(HttpResponseStatus.OK, content);
	}

	@Override
	public FullHttpResponse doPost(ChannelHandlerContext ctx, HttpRequest request, Map<String, List<String>> queryParameters, Map<String, List<String>> postParameters, Map<String, File> uploadFiles) throws RestException, IOException {

		validateRequestParameters(postParameters);

		List<String> accountIds = postParameters.get(REQUEST_INPUT_ACCOUNTS);
		String text = ListUtils.getFirst(postParameters.get(REQUEST_INPUT_TEXT));
		
		twitterManager.sendMessage(accountIds, text);

		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
		return httpResponseProvider.createHttpResponse(HttpResponseStatus.OK, HttpUtils.fromString("OK"));
	}

	protected void validateRequestParameters(Map<String, List<String>> requestParameters) throws RestException {
		if (StringUtils.nullOrEmpty(ListUtils.getFirst(requestParameters.get(REQUEST_INPUT_ACCOUNTS)))) {
			throw new RestException("Parameter is missing: " + REQUEST_INPUT_ACCOUNTS);
		}
		String text = ListUtils.getFirst(requestParameters.get(REQUEST_INPUT_TEXT));
		if (StringUtils.nullOrEmpty(text)) {
			throw new RestException("Parameter is missing: " + REQUEST_INPUT_TEXT);
		}
		if (text.length() > TWITTER_MAX_TEXT) {
			throw new RestException("Text length " + text.length() + " > " + TWITTER_MAX_TEXT);
		}
	}
	
}
