package com.euromoby.rest.handler.mail;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.http.HttpResponseProvider;
import com.euromoby.mail.MailAccount;
import com.euromoby.mail.MailManager;
import com.euromoby.mail.MailMessage;
import com.euromoby.model.Tuple;
import com.euromoby.rest.RestException;
import com.euromoby.rest.handler.RestHandlerBase;

@Component
public class MailDeleteHandler extends RestHandlerBase {

	public static final String URL_REGEXP = "/mail/box/([a-z0-9\\._-]+)/([a-z0-9\\._-]+)/delete/([0-9]+)";

	private MailManager mailManager;

	@Autowired
	public MailDeleteHandler(MailManager mailManager) {
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

		MailMessage mailMessage = mailManager.findMessage(mailAccount.getId(), Integer.parseInt(m.group(3)));
		if (mailMessage == null) {
			throw new RestException(HttpResponseStatus.NOT_FOUND);
		}

		mailManager.deleteMessage(loginDomain, mailMessage);

		String location = "/mail/box/" + loginDomain.getSecond() + "/" + loginDomain.getFirst();
		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);		
		return httpResponseProvider.createRedirectResponse(location);
	}

}
