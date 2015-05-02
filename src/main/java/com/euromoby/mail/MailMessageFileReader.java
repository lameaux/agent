package com.euromoby.mail;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.IOUtils;
import org.apache.commons.mail.util.MimeMessageParser;

public class MailMessageFileReader {
	private File file;

	public MailMessageFileReader(File file) {
		this.file = file;
	}

	public MimeMessageParser parseMessage() {
		try {
			Session s = Session.getDefaultInstance(new Properties());	
			FileInputStream is = new FileInputStream(file);
			try {
				MimeMessage message = new MimeMessage(s, is);
				MimeMessageParser parser = new MimeMessageParser(message);
				return parser.parse();
			} finally {
				IOUtils.closeQuietly(is);
			}
		} catch (Exception e) {
			return null;
		}
	}
}
