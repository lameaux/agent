package com.euromoby.mail.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.mail.MailManager;
import com.euromoby.mail.MailSession;
import com.euromoby.mail.util.DSNStatus;
import com.euromoby.model.Tuple;
import com.euromoby.utils.StringUtils;

@Component
public class RcptSmtpCommand extends SmtpCommandBase implements SmtpCommand {

	public static final String COMMAND_NAME = "RCPT";
	public static final Pattern TO = Pattern.compile("TO:\\s?<([^>]+)>");
	
	public static final String RESPONSE_503_NEED_MAIL = "503 " + DSNStatus.getStatus(DSNStatus.PERMANENT, DSNStatus.DELIVERY_OTHER) + " Need MAIL before RCPT";
	public static final String RESPONSE_501_SYNTAX_ERROR = "501 " + DSNStatus.getStatus(DSNStatus.PERMANENT, DSNStatus.DELIVERY_SYNTAX) + " Syntax error in parameters or arguments";
	public static final String RESPONSE_553_SYNTAX_ERROR_RECIPIENT = "553 " + DSNStatus.getStatus(DSNStatus.PERMANENT, DSNStatus.ADDRESS_SYNTAX) + " Syntax error in recipient address";
	public static final String RESPONSE_550_RECIPIENT_NOT_ALLOWED = "550 " + DSNStatus.getStatus(DSNStatus.PERMANENT, DSNStatus.SECURITY_AUTH) + " Recipient does not exist";
	public static final String RESPONSE_250_RECIPIENT_OK = "250 " + DSNStatus.getStatus(DSNStatus.SUCCESS, DSNStatus.ADDRESS_VALID) + " Recipient OK";
	
	private MailManager mailManager;
	
	@Autowired
	public RcptSmtpCommand(MailManager mailManager) {
		this.mailManager = mailManager;
		
	}
	
	@Override
	public String name() {
		return COMMAND_NAME;
	}

	@Override
	public String execute(MailSession mailSession, Tuple<String, String> request) {

		if (mailSession.getSender().isEmpty()) {
			return RESPONSE_503_NEED_MAIL;
		}

		String to = request.getSecond();
		if (StringUtils.nullOrEmpty(to)) {
			return RESPONSE_501_SYNTAX_ERROR;
		}
		to = to.trim();
		Matcher m = TO.matcher(to);
		if (!m.matches()) {
			return RESPONSE_501_SYNTAX_ERROR;
		}

		String recipientEmail = m.group(1);

		Tuple<String, String> recipient = Tuple.splitString(recipientEmail, "@");
		if (StringUtils.nullOrEmpty(recipient.getFirst()) || StringUtils.nullOrEmpty(recipient.getSecond())) {
			return RESPONSE_553_SYNTAX_ERROR_RECIPIENT;
		}

		if (!isAllowedRecipient(recipient)) {
			return RESPONSE_550_RECIPIENT_NOT_ALLOWED;
		}

		mailSession.setRecipient(recipient);

		return RESPONSE_250_RECIPIENT_OK;
	}

	protected boolean isAllowedRecipient(Tuple<String, String> recipient) {
		return mailManager.find(recipient) != null;
	}

}
