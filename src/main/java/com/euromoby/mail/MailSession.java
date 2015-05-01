package com.euromoby.mail;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.euromoby.model.Tuple;
import com.euromoby.utils.StringUtils;

public class MailSession {

	public static final int MAX_MESSAGE_SIZE = 10 * 1024 * 1024;

	private boolean commandMode = true;
	private String domain;
	private Tuple<String, String> sender = Tuple.empty();
	private Tuple<String, String> recipient = Tuple.empty();
	private int declaredMailSize = 0;
	private int realMailSize = 0;
	private boolean processingHeaders = true;
	private List<String> headers = new ArrayList<String>();
	private File tempFile = null;

	public boolean processDataLine(String line) throws Exception {
		realMailSize += line.length();
		if (realMailSize > MAX_MESSAGE_SIZE) {
			throw new MailSizeException();
		}

		if (tempFile == null) {
			tempFile = File.createTempFile("mail", ".msg");
			tempFile.deleteOnExit();
		}

		FileWriter fileWriter = new FileWriter(tempFile, true);
		try {
			if (processingHeaders) {
				if (StringUtils.nullOrEmpty(line)) {
					processingHeaders = false;
				} else {
					headers.add(line);
				}
				fileWriter.write(line);
				fileWriter.write(StringUtils.CRLF);
			} else {
				if (isEndOfTransfer(line)) {
					return true;
				}
				fileWriter.write(line);
				fileWriter.write(StringUtils.CRLF);
			}
		} finally {
			IOUtils.closeQuietly(fileWriter);
		}
		return false;
	}

	public boolean isEndOfTransfer(String line) {
		return ".".equals(line);
	}

	public void reset() {
		commandMode = true;
		domain = null;
		sender = Tuple.empty();
		recipient = Tuple.empty();
		declaredMailSize = 0;
		realMailSize = 0;
		processingHeaders = true;
		headers.clear();
		if (tempFile != null) {
			tempFile.delete();
			tempFile = null;
		}
	}

	public boolean isCommandMode() {
		return commandMode;
	}

	public void setCommandMode(boolean commandMode) {
		this.commandMode = commandMode;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public Tuple<String, String> getSender() {
		return sender;
	}

	public void setSender(Tuple<String, String> sender) {
		this.sender = sender;
	}

	public Tuple<String, String> getRecipient() {
		return recipient;
	}

	public void setRecipient(Tuple<String, String> recipient) {
		this.recipient = recipient;
	}

	public int getDeclaredMailSize() {
		return declaredMailSize;
	}

	public void setDeclaredMailSize(int declaredMailSize) {
		this.declaredMailSize = declaredMailSize;
	}

	public boolean isProcessingHeaders() {
		return processingHeaders;
	}

	public void setProcessingHeaders(boolean processingHeaders) {
		this.processingHeaders = processingHeaders;
	}

	public int getRealMailSize() {
		return realMailSize;
	}

	public void setRealMailSize(int realMailSize) {
		this.realMailSize = realMailSize;
	}

	public List<String> getHeaders() {
		return headers;
	}

	public void setHeaders(List<String> headers) {
		this.headers = headers;
	}

	public File getTempFile() {
		return tempFile;
	}

	public void setTempFile(File tempFile) {
		this.tempFile = tempFile;
	}

}
