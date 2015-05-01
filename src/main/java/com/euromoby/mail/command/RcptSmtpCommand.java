package com.euromoby.mail.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.euromoby.mail.MailSession;
import com.euromoby.mail.util.DSNStatus;
import com.euromoby.model.Tuple;
import com.euromoby.utils.StringUtils;

@Component
public class RcptSmtpCommand extends SmtpCommandBase implements SmtpCommand {

	public static final String COMMAND_NAME = "RCPT";
	public static final Pattern TO = Pattern.compile("TO:\\s?<([^>]+)>");

	@Override
	public String name() {
		return COMMAND_NAME;
	}

	@Override
	public String execute(MailSession mailSession, Tuple<String, String> request) {

		if (mailSession.getSender() == null || mailSession.getSender().isEmpty()) {
			return "503 " + DSNStatus.getStatus(DSNStatus.PERMANENT, DSNStatus.DELIVERY_OTHER) + " Need MAIL before RCPT";
		}

		String to = request.getSecond();
		if (StringUtils.nullOrEmpty(to)) {
			return "501 " + DSNStatus.getStatus(DSNStatus.PERMANENT, DSNStatus.DELIVERY_SYNTAX) + " Usage: RCPT TO:<recipient>";
		}
		to = to.trim();
		Matcher m = TO.matcher(to);
		if (!m.matches()) {
			return "501 " + DSNStatus.getStatus(DSNStatus.PERMANENT, DSNStatus.DELIVERY_SYNTAX) + " Syntax error in parameters or arguments";
		}

		String recipientEmail = m.group(1);

		Tuple<String, String> recipient = Tuple.splitString(recipientEmail, "@");
		if (StringUtils.nullOrEmpty(recipient.getFirst()) || StringUtils.nullOrEmpty(recipient.getSecond())) {
			return "553 " + DSNStatus.getStatus(DSNStatus.PERMANENT, DSNStatus.ADDRESS_SYNTAX) + " Syntax error in recipient address";
		}

		if (!isAllowedRecipient(recipient)) {
			return "550 " + DSNStatus.getStatus(DSNStatus.PERMANENT, DSNStatus.SECURITY_AUTH) + " Recipient does not exist";
		}

		mailSession.setRecipient(recipient);

		return "250 " + DSNStatus.getStatus(DSNStatus.SUCCESS, DSNStatus.ADDRESS_VALID) + " Recipient <" + recipientEmail + "> OK";
	}

	protected boolean isAllowedRecipient(Tuple<String, String> recipient) {
		return true;
	}

}
