package com.euromoby.mail;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.mail.command.SmtpCommand;
import com.euromoby.mail.util.DSNStatus;
import com.euromoby.model.Tuple;

@Component
public class SmtpCommandProcessor {

	public static final String INVALID_COMMAND = "500 " + DSNStatus.getStatus(DSNStatus.PERMANENT, DSNStatus.DELIVERY_INVALID_CMD) + " Invalid command";
	
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
			return INVALID_COMMAND;
		} catch (Exception e) {
			return "500 Error: " + e.getMessage();
		}
	}	
	

	
	/*
            cmds.setProperty("AUTH",AuthCmdHandler.class.getName());
            cmds.setProperty("DATA",DataCmdHandler.class.getName());
            cmds.setProperty("EXPN",ExpnCmdHandler.class.getName());
            cmds.setProperty("MAIL",MailCmdHandler.class.getName());
            cmds.setProperty("RCPT" ,RcptCmdHandler.class.getName());
            cmds.setProperty("RSET",RsetCmdHandler.class.getName());
            cmds.setProperty("VRFY",VrfyCmdHandler.class.getName());
            
	 */
	
}
