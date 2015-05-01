package com.euromoby.mail;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.Config;
import com.euromoby.model.Tuple;

@Component
public class MailFileProvider {

	private Config config;

	@Autowired
	public MailFileProvider(Config config) {
		this.config = config;
	}

	public File getInboxDirectory(Tuple<String, String> recipient) throws Exception {
		String location = recipient.getSecond() + File.separator + recipient.getFirst();
		File targetDir = new File(new File(config.getAgentMailPath()), location);
		if (!targetDir.exists() && !targetDir.mkdirs() && !targetDir.mkdir()) {
			throw new Exception("Error saving file to " + targetDir.getAbsolutePath());
		}
		return targetDir;
	}
	
	public File getNewTargetFile(Tuple<String, String> recipient, int messageId) throws Exception {
		File inboxDirectory = getInboxDirectory(recipient);
		return new File(inboxDirectory, messageId + ".msg");
	}
	
}
