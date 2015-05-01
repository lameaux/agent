package com.euromoby.mail.command;

import com.euromoby.mail.MailSession;
import com.euromoby.mail.util.DSNStatus;
import com.euromoby.model.Tuple;
import com.euromoby.utils.StringUtils;

public abstract class SmtpCommandBase implements SmtpCommand {

	@Override
	public abstract String name();

	@Override
	public String execute(MailSession mailSession, Tuple<String, String> request) {
		return "502 " + DSNStatus.getStatus(DSNStatus.PERMANENT, DSNStatus.SYSTEM_NOT_CAPABLE) + " Command is not supported";
	}

	@Override
	public boolean match(Tuple<String, String> request) {
		if (StringUtils.nullOrEmpty(request.getFirst())) {
			return false;
		}
		return name().equalsIgnoreCase(request.getFirst());
	}

}
