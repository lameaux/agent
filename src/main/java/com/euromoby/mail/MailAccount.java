package com.euromoby.mail;

public class MailAccount {

	private String user;
	private String domain;

	public MailAccount() {

	}

	public MailAccount(String user, String domain) {
		this.user = user;
		this.domain = domain;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

}
