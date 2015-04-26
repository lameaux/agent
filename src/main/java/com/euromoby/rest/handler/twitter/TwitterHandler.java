package com.euromoby.rest.handler.twitter;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.http.HttpResponseProvider;
import com.euromoby.http.HttpUtils;
import com.euromoby.rest.RestException;
import com.euromoby.rest.handler.RestHandlerBase;
import com.euromoby.twitter.TwitterProvider;
import com.euromoby.utils.IOUtils;
import com.euromoby.utils.StringUtils;

@Component
public class TwitterHandler extends RestHandlerBase {
	
	public static final String URL = "/twitter";
	private static final String REQUEST_INPUT_SCREEN_NAME = "screen_name";
	private static final String REQUEST_INPUT_TEXT = "text";
	private static final int TWITTER_MAX_TEXT = 140;

	private TwitterProvider twitterProvider;

	
	@Autowired
	public TwitterHandler(TwitterProvider twitterProvider) {
		this.twitterProvider = twitterProvider;
	}	
	
	@Override
	public boolean matchUri(URI uri) {
		return uri.getPath().equals(URL);
	}	

	protected String getOptionListOfScreenNames() {
		StringBuffer sb = new StringBuffer();
		for (String screenName : twitterProvider.getScreenNames()) {
			sb.append("<option value=\"").append(screenName).append("\">");
			sb.append(screenName);
			sb.append("</option>");
		}
		return sb.toString();
	}	

	@Override
	public FullHttpResponse doGet() {
		InputStream is = TwitterHandler.class.getResourceAsStream("twitter.html");
		String pageContent = IOUtils.streamToString(is);
		pageContent = pageContent.replace("%SCREEN_NAMES%", getOptionListOfScreenNames());
		ByteBuf content = Unpooled.copiedBuffer(pageContent, CharsetUtil.UTF_8);
		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
		return httpResponseProvider.createHttpResponse(HttpResponseStatus.OK, content);
	}

	@Override
	public FullHttpResponse doPost() throws RestException, IOException {

		Map<String, String> requestParameters = getRequestParameters();
		validateRequestParameters(requestParameters);

		String screenName = requestParameters.get(REQUEST_INPUT_SCREEN_NAME);
		String text = requestParameters.get(REQUEST_INPUT_TEXT);
		
		try {
			twitterProvider.sendMessage(screenName, text);
		} catch (Exception e) {
			throw new RestException(e);
		}

		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
		return httpResponseProvider.createHttpResponse(HttpResponseStatus.OK, HttpUtils.fromString("OK"));
	}

	protected void validateRequestParameters(Map<String, String> requestParameters) throws RestException {
		if (StringUtils.nullOrEmpty(requestParameters.get(REQUEST_INPUT_SCREEN_NAME))) {
			throw new RestException("Parameter is missing: " + REQUEST_INPUT_SCREEN_NAME);
		}
		String text = requestParameters.get(REQUEST_INPUT_TEXT);
		if (StringUtils.nullOrEmpty(text)) {
			throw new RestException("Parameter is missing: " + REQUEST_INPUT_TEXT);
		}
		if (text.length() > TWITTER_MAX_TEXT) {
			throw new RestException("Text length " + text.length() + " > " + TWITTER_MAX_TEXT);
		}
	}
	
}
