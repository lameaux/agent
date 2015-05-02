package com.euromoby.mail;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.dao.MailAccountDao;
import com.euromoby.dao.MailMessageDao;
import com.euromoby.model.Tuple;

@Component
public class MailManager {

	private static final Logger log = LoggerFactory.getLogger(MailManager.class);

	private MailAccountDao mailAccountDao;
	private MailMessageDao mailMessageDao;
	private MailFileProvider mailFileProvider;

	@Autowired
	public MailManager(MailAccountDao mailAccountDao, MailMessageDao mailMessageDao, MailFileProvider mailFileProvider) {
		this.mailAccountDao = mailAccountDao;
		this.mailMessageDao = mailMessageDao;
		this.mailFileProvider = mailFileProvider;
	}

	public MailAccount findAccount(Tuple<String, String> loginDomain) {
			return mailAccountDao.findByLoginAndDomain(loginDomain.getFirst(), loginDomain.getSecond());
	}

	public MailMessage findMessage(Integer accountId, Integer id) {
		return mailMessageDao.findByAccountIdAndId(accountId, id);
}	
	
	public void saveAccount(MailAccount mailAccount) {
		mailAccountDao.save(mailAccount);
	}
	
	public List<MailAccount> getAccounts() {
		return mailAccountDao.findAll();
	}

	public List<MailMessage> getMessages(Integer accountId) {
		return mailMessageDao.findByAccountId(accountId);
	}
	
	public void saveMessage(MailSession mailSession) {
		Tuple<String, String> recipient = mailSession.getRecipient();
		
		MailAccount account = mailAccountDao.findByLoginAndDomain(recipient.getFirst(), recipient.getSecond());
		MailMessage mailMessage = new MailMessage();
		mailMessage.setAccountId(account.getId());
		mailMessage.setSender(mailSession.getSender().getFirst() + "@" + mailSession.getSender().getSecond());
		mailMessage.setSize(mailSession.getRealMailSize());
		mailMessage.setCreated(new Date());
		mailMessageDao.save(mailMessage);
		
		try {
			File targetFile = mailFileProvider.getMessageFile(recipient, mailMessage.getId());
			FileUtils.copyFile(mailSession.getTempFile(), targetFile);
		} catch (Exception e) {
			log.error("Internal Error", e);
		}

	}

}
