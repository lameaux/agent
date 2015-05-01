package com.euromoby.mail;

public class MailAccount {

	private Integer id;
	private String login;
	private String domain;

	public MailAccount() {

	}

	public MailAccount(Integer id, String login, String domain) {
		this.id = id;
		this.login = login;
		this.domain = domain;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}



}
