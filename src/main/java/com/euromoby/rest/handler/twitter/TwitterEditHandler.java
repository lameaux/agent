package com.euromoby.rest.handler.twitter;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

@Component
public class TwitterEditHandler extends RestHandlerBase {

	public static final String URL_REGEXP = "/twitter/edit/([0-9]+)";

	public static final String REQUEST_INPUT_TAGS = "tags";
	
	private TwitterManager twitterManager;
	
	@Autowired
	public TwitterEditHandler(TwitterManager twitterManager) {
		this.twitterManager = twitterManager;
	}
	
	@Override
	public boolean matchUri(URI uri) {
		return uri.getPath().matches(URL_REGEXP);
	}
	
	@Override
	public FullHttpResponse doGet(ChannelHandlerContext ctx, HttpRequest request, Map<String, List<String>> queryParameters) throws RestException {
		
		URI uri = getUri(request);
		
		Pattern p = Pattern.compile(URL_REGEXP);
		Matcher m = p.matcher(uri.getPath());
		if (!m.matches()) {
			throw new RestException(HttpResponseStatus.BAD_REQUEST);
		}

		String userId = m.group(1);
		TwitterAccount twitterAccount = twitterManager.getAccountById(userId);
		if (twitterAccount == null) {
			throw new RestException(HttpResponseStatus.NOT_FOUND);
		}		
		
		InputStream is = TwitterEditHandler.class.getResourceAsStream("twitteredit.html");
		String pageContent = IOUtils.streamToString(is);
		
		pageContent = pageContent.replace("%ID%", String.valueOf(twitterAccount.getId()));
		pageContent = pageContent.replace("%SCREEN_NAME%", twitterAccount.getScreenName());
		pageContent = pageContent.replace("%TAGS%", StringUtils.emptyStringIfNull(twitterAccount.getTags()));		
		
		ByteBuf content = Unpooled.copiedBuffer(pageContent, CharsetUtil.UTF_8);
		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
		return httpResponseProvider.createHttpResponse(HttpResponseStatus.OK, content);
	}

	@Override
	public FullHttpResponse doPost(ChannelHandlerContext ctx, HttpRequest request, Map<String, List<String>> queryParameters, Map<String, List<String>> postParameters, Map<String, File> uploadFiles) throws RestException {

		URI uri = getUri(request);
		
		Pattern p = Pattern.compile(URL_REGEXP);
		Matcher m = p.matcher(uri.getPath());
		if (!m.matches()) {
			throw new RestException(HttpResponseStatus.BAD_REQUEST);
		}

		String userId = m.group(1);
		TwitterAccount twitterAccount = twitterManager.getAccountById(userId);
		if (twitterAccount == null) {
			throw new RestException(HttpResponseStatus.NOT_FOUND);
		}		
		
		validateRequestParameters(postParameters);

		twitterAccount.setTags(ListUtils.getFirst(postParameters.get(REQUEST_INPUT_TAGS)));
		twitterManager.updateAccount(twitterAccount);

		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
		return httpResponseProvider.createHttpResponse(HttpResponseStatus.OK, HttpUtils.fromString("OK"));
	}

	protected void validateRequestParameters(Map<String, List<String>> requestParameters) throws RestException {
		if (StringUtils.nullOrEmpty(ListUtils.getFirst(requestParameters.get(REQUEST_INPUT_TAGS)))) {
			throw new RestException("Parameter is missing: " + REQUEST_INPUT_TAGS);
		}
	}
	

}
