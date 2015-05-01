package com.euromoby.mail.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.euromoby.mail.MailSession;
import com.euromoby.mail.util.DSNStatus;
import com.euromoby.model.Tuple;
import com.euromoby.utils.StringUtils;

@Component
public class MailSmtpCommand extends SmtpCommandBase implements SmtpCommand {

	public static final String COMMAND_NAME = "MAIL";
	public static final Pattern FROM_SIZE = Pattern.compile("FROM:\\s?<([^>]+)>\\s?(SIZE=([0-9]+))?");

	@Override
	public String name() {
		return COMMAND_NAME;
	}

	@Override
	public String execute(MailSession mailSession, Tuple<String, String> request) {

		if (StringUtils.nullOrEmpty(mailSession.getDomain())) {
			return "503 " + DSNStatus.getStatus(DSNStatus.PERMANENT, DSNStatus.DELIVERY_OTHER) + " Need HELO or EHLO before MAIL";
		}

		String fromSize = request.getSecond();
		if (StringUtils.nullOrEmpty(fromSize)) {
			return "501 " + DSNStatus.getStatus(DSNStatus.PERMANENT, DSNStatus.ADDRESS_SYNTAX_SENDER) + " Syntax error in MAIL command";
		}
		fromSize = fromSize.trim();
		Matcher m = FROM_SIZE.matcher(fromSize);
		if (!m.matches()) {
			return "501 " + DSNStatus.getStatus(DSNStatus.PERMANENT, DSNStatus.ADDRESS_SYNTAX_SENDER) + " Syntax error in MAIL command";
		}

		String senderEmail = m.group(1);
		int mailSize = 0;
		if (m.group(3) != null) {
			mailSize = Integer.parseInt(m.group(3));
		}

		Tuple<String, String> sender = Tuple.splitString(senderEmail, "@");
		if (StringUtils.nullOrEmpty(sender.getFirst()) || StringUtils.nullOrEmpty(sender.getSecond())) {
			return "501 " + DSNStatus.getStatus(DSNStatus.PERMANENT, DSNStatus.ADDRESS_SYNTAX_SENDER) + " Syntax error in sender address";
		}

		if (!isAllowedSender(sender)) {
			return "501 " + DSNStatus.getStatus(DSNStatus.PERMANENT, DSNStatus.ADDRESS_SYNTAX_SENDER) + " Sender is not allowed";
		}

		mailSession.setSender(sender);

		if (mailSize > 0 && mailSize > MailSession.MAX_MESSAGE_SIZE) {
			return "552 " + DSNStatus.getStatus(DSNStatus.PERMANENT, DSNStatus.SYSTEM_MSG_TOO_BIG) + " Message size exceeds fixed maximum message size";
		}
		mailSession.setDeclaredMailSize(mailSize);

		return "250 " + DSNStatus.getStatus(DSNStatus.SUCCESS, DSNStatus.ADDRESS_OTHER) + " Sender <" + senderEmail + "> OK";
	}

	protected boolean isAllowedSender(Tuple<String, String> sender) {
		return true;
	}

}
