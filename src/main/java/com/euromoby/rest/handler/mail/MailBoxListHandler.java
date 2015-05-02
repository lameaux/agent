package com.euromoby.rest.handler.mail;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
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
import com.euromoby.utils.DateUtils;
import com.euromoby.utils.IOUtils;

@Component
public class MailBoxListHandler extends RestHandlerBase {

	public static final String URL_REGEXP = "/mail/box/([a-z0-9\\._-]+)/([a-z0-9\\._-]+)";

	private MailManager mailManager;

	@Autowired
	public MailBoxListHandler(MailManager mailManager) {
		this.mailManager = mailManager;
	}

	@Override
	public boolean matchUri(URI uri) {
		return uri.getPath().matches(URL_REGEXP);
	}

	@Override
	public FullHttpResponse doGet() throws RestException {

		URI uri = getUri();
		
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

		InputStream is = MailBoxListHandler.class.getResourceAsStream("mailboxlist.html");
		String pageContent = IOUtils.streamToString(is);

		pageContent = pageContent.replace("%MAIL_ACCOUNT%", mailAccount.getLogin() + "@" + mailAccount.getDomain());
		
		List<MailMessage> messages = mailManager.getMessages(mailAccount.getId());
		StringBuffer sb = new StringBuffer();

		for (MailMessage message : messages) {
			sb.append("<tr>");
			sb.append("<td>").append(message.getSender()).append("</td>");
			sb.append("<td>").append(DateUtils.iso(message.getCreated().getTime())).append("</td>");
			sb.append("<td>").append(message.getSize()).append("</td>");
			sb.append("<td><a href=\"/mail/box/");
			sb.append(mailAccount.getDomain());
			sb.append("/");
			sb.append(mailAccount.getLogin());
			sb.append("/view/");
			sb.append(message.getId());
			sb.append("\">Read</a> | Delete</td>");
			sb.append("</tr>");
		}
		pageContent = pageContent.replace("%MESSAGES_LIST%", sb.toString());

		ByteBuf content = Unpooled.copiedBuffer(pageContent, CharsetUtil.UTF_8);
		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
		return httpResponseProvider.createHttpResponse(HttpResponseStatus.OK, content);
	}

}
