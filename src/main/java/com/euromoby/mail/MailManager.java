package com.euromoby.mail;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.model.Tuple;
import com.euromoby.utils.StringUtils;

@Component
public class MailManager {

	private static final Logger log = LoggerFactory.getLogger(MailManager.class);

	private List<MailAccount> accounts = Arrays.asList(new MailAccount("info", "dirty.to"));

	private MailFileProvider mailFileProvider;

	@Autowired
	public MailManager(MailFileProvider mailFileProvider) {
		this.mailFileProvider = mailFileProvider;
	}

	public MailAccount find(Tuple<String, String> userDomain) {
		for (MailAccount account : accounts) {
			if (account.getUser().equals(userDomain.getFirst()) && account.getDomain().equals(userDomain.getSecond())) {
				return account;
			}
		}
		return null;
	}

	public List<MailAccount> getAccounts() {
		return new ArrayList<MailAccount>(accounts);
	}

	public void save(MailSession mailSession) {

		Tuple<String, String> recipient = mailSession.getRecipient();
		try {
			
			File targetFile = mailFileProvider.getNewTargetFile(recipient);

			FileWriter fileWriter = new FileWriter(targetFile);
			try {
				for (String header : mailSession.getHeaders()) {
					fileWriter.write(header);
					fileWriter.write(StringUtils.CRLF);
				}
				fileWriter.write(StringUtils.CRLF);
				for (String body : mailSession.getBody()) {
					fileWriter.write(body);
					fileWriter.write(StringUtils.CRLF);
				}
			} finally {
				IOUtils.closeQuietly(fileWriter);
			}

		} catch (Exception e) {
			log.error("Internal Error", e);
		}

	}

}
