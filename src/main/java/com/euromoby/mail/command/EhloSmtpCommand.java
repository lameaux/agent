package com.euromoby.mail.command;

import org.springframework.stereotype.Component;

import com.euromoby.mail.MailSession;
import com.euromoby.mail.util.DSNStatus;
import com.euromoby.model.Tuple;
import com.euromoby.utils.StringUtils;

@Component
public class EhloSmtpCommand extends SmtpCommandBase implements SmtpCommand {

	public static final String COMMAND_NAME = "EHLO";
	public static final int MAX_MESSAGE_SIZE = 10 * 1024 * 1024;

	@Override
	public String name() {
		return COMMAND_NAME;
	}

	@Override
	public String execute(MailSession mailSession, Tuple<String, String> request) {

		String domain = request.getSecond();
		if (StringUtils.nullOrEmpty(domain)) {
			return "501 " + DSNStatus.getStatus(DSNStatus.PERMANENT, DSNStatus.DELIVERY_INVALID_ARG) + " Invalid domain name";
		}
		mailSession.setDomain(domain);

		StringBuffer sb = new StringBuffer();
		sb.append("250-Hello ").append(domain).append(StringUtils.CRLF);
		sb.append("250-SIZE " + MAX_MESSAGE_SIZE).append(StringUtils.CRLF);
		sb.append("250-PIPELINING").append(StringUtils.CRLF);
		sb.append("250 ENHANCEDSTATUSCODES");

		return sb.toString();
	}

}
