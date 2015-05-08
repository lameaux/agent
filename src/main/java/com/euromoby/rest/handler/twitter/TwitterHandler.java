package com.euromoby.rest.handler.twitter;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.http.HttpResponseProvider;
import com.euromoby.rest.handler.RestHandlerBase;
import com.euromoby.twitter.TwitterAccount;
import com.euromoby.twitter.TwitterManager;
import com.euromoby.utils.IOUtils;
import com.euromoby.utils.StringUtils;

@Component
public class TwitterHandler extends RestHandlerBase {
	
	public static final String URL = "/twitter";

	private TwitterManager twitterManager;
	
	@Autowired
	public TwitterHandler(TwitterManager twitterManager) {
		this.twitterManager = twitterManager;
	}	
	
	@Override
	public boolean matchUri(URI uri) {
		return uri.getPath().equals(URL);
	}	

	@Override
	public FullHttpResponse doGet(ChannelHandlerContext ctx, HttpRequest request, Map<String, List<String>> queryParameters) {
		InputStream is = TwitterHandler.class.getResourceAsStream("twitter.html");
		String pageContent = IOUtils.streamToString(is);

		List<TwitterAccount> accounts = twitterManager.getAccounts();
		StringBuffer sb = new StringBuffer();

		for (TwitterAccount account : accounts) {
			sb.append("<tr>");
			sb.append("<td>").append(account.getScreenName()).append("</td>");
			sb.append("<td>").append(StringUtils.emptyStringIfNull(account.getTags())).append("</td>");
			sb.append("<td><a href=\"/twitter/edit/");
			sb.append(account.getId());
			sb.append("\">Edit</a></td>");				
			sb.append("</tr>");
		}
		pageContent = pageContent.replace("%TWITTER_LIST%", sb.toString());

		ByteBuf content = Unpooled.copiedBuffer(pageContent, CharsetUtil.UTF_8);
		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
		return httpResponseProvider.createHttpResponse(HttpResponseStatus.OK, content);
	}
	
}
