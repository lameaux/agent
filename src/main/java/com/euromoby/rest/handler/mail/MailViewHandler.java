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

import org.apache.commons.mail.util.MimeMessageParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.http.HttpResponseProvider;
import com.euromoby.mail.MailAccount;
import com.euromoby.mail.MailFileProvider;
import com.euromoby.mail.MailManager;
import com.euromoby.mail.MailMessage;
import com.euromoby.mail.MailMessageFileReader;
import com.euromoby.model.Tuple;
import com.euromoby.rest.RestException;
import com.euromoby.rest.handler.RestHandlerBase;
import com.euromoby.utils.DateUtils;
import com.euromoby.utils.IOUtils;

@Component
public class MailViewHandler extends RestHandlerBase {

	public static final String URL_REGEXP = "/mail/box/([a-z0-9\\._-]+)/([a-z0-9\\._-]+)/view/([0-9]+)";

	private MailManager mailManager;
	private MailFileProvider mailFileProvider;

	@Autowired
	public MailViewHandler(MailManager mailManager, MailFileProvider mailFileProvider) {
		this.mailManager = mailManager;
		this.mailFileProvider = mailFileProvider;
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

		InputStream is = MailViewHandler.class.getResourceAsStream("mailview.html");
		String pageContent = IOUtils.streamToString(is);

		File mailFile = null;
		try {
			mailFile = mailFileProvider.getMessageFile(loginDomain, mailMessage.getId());
		} catch (Exception e) {
			throw new RestException(HttpResponseStatus.INTERNAL_SERVER_ERROR, e);
		}
		
		MailMessageFileReader messageReader = new MailMessageFileReader(mailFile);
		MimeMessageParser parser = messageReader.parseMessage();
		if (parser == null) {
			throw new RestException(HttpResponseStatus.INTERNAL_SERVER_ERROR);
		}

		pageContent = pageContent.replace("%DATE%", DateUtils.iso(mailMessage.getCreated().getTime()));		
		try {
			pageContent = pageContent.replace("%SUBJECT%", parser.getSubject());
			pageContent = pageContent.replace("%FROM%", parser.getFrom());
			
			if (parser.hasHtmlContent()) {
				pageContent = pageContent.replace("%BODY%", parser.getHtmlContent());
			} else {
				pageContent = pageContent.replace("%BODY%", "<pre>" + parser.getPlainContent() + "</pre>");
			}
		} catch (Exception e) {
			throw new RestException(HttpResponseStatus.INTERNAL_SERVER_ERROR, e);			
		}
		
		ByteBuf content = Unpooled.copiedBuffer(pageContent, CharsetUtil.UTF_8);
		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
		return httpResponseProvider.createHttpResponse(HttpResponseStatus.OK, content);
	}

}
