package com.euromoby.mail.command;

import com.euromoby.mail.MailSession;
import com.euromoby.model.Tuple;

public interface SmtpCommand {
	
	public static final String SEPARATOR = " ";

	String name();
	
	String execute(MailSession mailSession, Tuple<String, String> request);

	boolean match(Tuple<String, String> request);


}
