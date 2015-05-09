package com.euromoby.rest.handler.mail;

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
import com.euromoby.mail.MailManager;
import com.euromoby.mail.model.MailAccount;
import com.euromoby.model.Tuple;
import com.euromoby.rest.RestException;
import com.euromoby.rest.handler.RestHandlerBase;
import com.euromoby.utils.IOUtils;
import com.euromoby.utils.ListUtils;
import com.euromoby.utils.StringUtils;

@Component
public class MailEditHandler extends RestHandlerBase {

	public static final String URL_REGEXP = "/mail/edit/([a-z0-9\\._-]+)/([a-z0-9\\._-]+)";

	public static final String REQUEST_INPUT_ACTIVE = "active";
	
	private MailManager mailManager;
	
	@Autowired
	public MailEditHandler(MailManager mailManager) {
		this.mailManager = mailManager;
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

		Tuple<String, String> loginDomain = Tuple.of(m.group(2), m.group(1));
		MailAccount mailAccount = mailManager.findAccount(loginDomain);
		if (mailAccount == null) {
			throw new RestException(HttpResponseStatus.NOT_FOUND);
		}		
		
		InputStream is = MailEditHandler.class.getResourceAsStream("mailedit.html");
		String pageContent = IOUtils.streamToString(is);
		
		pageContent = pageContent.replace("%LOGIN%", mailAccount.getLogin());
		pageContent = pageContent.replace("%DOMAIN%", mailAccount.getDomain());
		pageContent = pageContent.replace("%ACTIVE%", String.valueOf(mailAccount.getActive()));		
		
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

		Tuple<String, String> loginDomain = Tuple.of(m.group(2), m.group(1));
		MailAccount mailAccount = mailManager.findAccount(loginDomain);
		if (mailAccount == null) {
			throw new RestException(HttpResponseStatus.NOT_FOUND);
		}		
		
		validateRequestParameters(postParameters);

		mailAccount.setActive(Boolean.valueOf(ListUtils.getFirst(postParameters.get(REQUEST_INPUT_ACTIVE))));
		mailManager.updateAccount(mailAccount);

		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
		return httpResponseProvider.createHttpResponse(HttpResponseStatus.OK, HttpUtils.fromString("OK"));
	}

	protected void validateRequestParameters(Map<String, List<String>> requestParameters) throws RestException {
		if (StringUtils.nullOrEmpty(ListUtils.getFirst(requestParameters.get(REQUEST_INPUT_ACTIVE)))) {
			throw new RestException("Parameter is missing: " + REQUEST_INPUT_ACTIVE);
		}
	}
	

}
