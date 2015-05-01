package com.euromoby.mail;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.mail.command.SmtpCommand;
import com.euromoby.mail.util.DSNStatus;
import com.euromoby.model.Tuple;

@Component
public class SmtpCommandProcessor {

	public static final String RESPONSE_500_INVALID_COMMAND = "500 " + DSNStatus.getStatus(DSNStatus.PERMANENT, DSNStatus.DELIVERY_INVALID_CMD) + " Invalid command";
	public static final String RESPONSE_500_UNKNOWN_ERROR = "500 " + DSNStatus.getStatus(DSNStatus.PERMANENT, DSNStatus.DELIVERY_INVALID_CMD) + " Unknown error";

	private static final Logger LOG = LoggerFactory.getLogger(SmtpCommandProcessor.class);
	
	private List<SmtpCommand> commands;

	@Autowired
	public void setCommands(List<SmtpCommand> commands) {
		this.commands = commands;
	}

	public String process(MailSession mailSession, Tuple<String, String> request) {
		try {
			for (SmtpCommand command : commands) {
				if (command.match(request)) {
					return command.execute(mailSession, request);
				}
			}
			return RESPONSE_500_INVALID_COMMAND;
		} catch (Exception e) {
			LOG.error("Command Error", e);
			return RESPONSE_500_UNKNOWN_ERROR;
		}
	}
}
