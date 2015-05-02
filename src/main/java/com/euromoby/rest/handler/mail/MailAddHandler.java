package com.euromoby.rest.handler.mail;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.http.HttpResponseProvider;
import com.euromoby.http.HttpUtils;
import com.euromoby.mail.MailAccount;
import com.euromoby.mail.MailManager;
import com.euromoby.rest.RestException;
import com.euromoby.rest.handler.RestHandlerBase;
import com.euromoby.utils.IOUtils;
import com.euromoby.utils.StringUtils;

@Component
public class MailAddHandler extends RestHandlerBase {

	public static final String URL = "/mail/add";

	public static final String REQUEST_INPUT_LOGIN = "login";
	public static final String REQUEST_INPUT_DOMAIN = "domain";
	
	private MailManager mailManager;
	
	@Autowired
	public MailAddHandler(MailManager mailManager) {
		this.mailManager = mailManager;
	}
	
	@Override
	public boolean matchUri(URI uri) {
		return uri.getPath().equals(URL);
	}
	
	@Override
	public FullHttpResponse doGet() {
		InputStream is = MailAddHandler.class.getResourceAsStream("mailadd.html");
		String pageContent = IOUtils.streamToString(is);
		ByteBuf content = Unpooled.copiedBuffer(pageContent, CharsetUtil.UTF_8);
		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
		return httpResponseProvider.createHttpResponse(HttpResponseStatus.OK, content);
	}

	@Override
	public FullHttpResponse doPost() throws RestException {

		Map<String, String> requestParameters = getRequestParameters();

		validateRequestParameters(requestParameters);

		MailAccount mailAccount = new MailAccount();
		mailAccount.setLogin(requestParameters.get(REQUEST_INPUT_LOGIN));
		mailAccount.setDomain(requestParameters.get(REQUEST_INPUT_DOMAIN));
		mailManager.saveAccount(mailAccount);

		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
		return httpResponseProvider.createHttpResponse(HttpResponseStatus.OK, HttpUtils.fromString("OK"));
	}

	protected void validateRequestParameters(Map<String, String> requestParameters) throws RestException {
		if (StringUtils.nullOrEmpty(requestParameters.get(REQUEST_INPUT_LOGIN))) {
			throw new RestException("Parameter is missing: " + REQUEST_INPUT_LOGIN);
		}
		if (StringUtils.nullOrEmpty(requestParameters.get(REQUEST_INPUT_DOMAIN))) {
			throw new RestException("Parameter is missing: " + REQUEST_INPUT_DOMAIN);
		}
	}
	

}
