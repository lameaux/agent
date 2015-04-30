package com.euromoby.mail;

public class MailSession {

	private boolean commandMode = true;
	private String domain;

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

}
