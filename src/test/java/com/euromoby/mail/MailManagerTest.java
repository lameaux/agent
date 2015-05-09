package com.euromoby.mail;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.euromoby.mail.dao.MailAccountDao;
import com.euromoby.mail.dao.MailMessageDao;
import com.euromoby.mail.model.MailAccount;
import com.euromoby.mail.model.MailMessage;
import com.euromoby.model.Tuple;

@RunWith(MockitoJUnitRunner.class)
public class MailManagerTest {

	@Mock
	MailAccountDao mailAccountDao;
	@Mock
	MailMessageDao mailMessageDao;
	@Mock
	MailFileProvider mailFileProvider;	
	@Mock
	File mailFile;
	
	MailManager mailManager;

	@Before
	public void init() {
		mailManager = new MailManager(mailAccountDao, mailMessageDao, mailFileProvider);
	}

	@Test
	public void testFindAccount() {
		Tuple<String, String> account = Tuple.of("foo", "bar");
		mailManager.findAccount(account);
		Mockito.verify(mailAccountDao).findByLoginAndDomain(Matchers.eq(account.getFirst()), Matchers.eq(account.getSecond()));
	}

	@Test
	public void testFindMessage() {
		Integer accountId = 1;
		Integer id = 1;
		mailManager.findMessage(accountId, id);
		Mockito.verify(mailMessageDao).findByAccountIdAndId(Matchers.eq(accountId), Matchers.eq(id));
	}
	
	@Test
	public void testSaveAccount() {
		MailAccount mailAccount = new MailAccount();
		mailManager.saveAccount(mailAccount);
		Mockito.verify(mailAccountDao).save(Matchers.eq(mailAccount));
	}

	@Test
	public void testUpdateAccount() {
		MailAccount mailAccount = new MailAccount();
		mailManager.updateAccount(mailAccount);
		Mockito.verify(mailAccountDao).update(Matchers.eq(mailAccount));
	}	

	@Test
	public void testGetAccounts() {
		mailManager.getAccounts();
		Mockito.verify(mailAccountDao).findAll();
	}	

	@Test
	public void testGetMessages() {
		Integer accountId = 1;
		mailManager.getMessages(accountId);
		Mockito.verify(mailMessageDao).findByAccountId(Matchers.eq(accountId));
	}	

	@Test
	public void testDeleteMessageAndFile() throws Exception {
		Tuple<String, String> account = Tuple.of("foo", "bar");
		MailMessage mailMessage = new MailMessage();
		mailMessage.setId(1);
		Mockito.when(mailFileProvider.getMessageFile(Matchers.eq(account), Matchers.eq(mailMessage.getId()))).thenReturn(mailFile);
		
		mailManager.deleteMessage(account, mailMessage);
		Mockito.verify(mailMessageDao).deleteById(Matchers.eq(mailMessage.getId()));
		Mockito.verify(mailFile).delete();
	}	

	@Test
	public void testSaveMessage() throws Exception {
		Tuple<String, String> recipient = Tuple.of("foo", "bar");
		Tuple<String, String> sender = Tuple.of("foofoo", "barbar");		
		Integer accountId = 1;
		int REAL_MAIL_SIZE = 123;
		String BODY_LINE = "Hello World!";
		
		File sessionFile = File.createTempFile("foo", "bar");
		sessionFile.deleteOnExit();
		FileUtils.writeStringToFile(sessionFile, BODY_LINE);

		File targetFile = File.createTempFile("foo", "bar");
		targetFile.deleteOnExit();
		

		MailSession mailSession = new MailSession();
		mailSession.setSender(sender);
		mailSession.setRecipient(recipient);
		mailSession.setRealMailSize(REAL_MAIL_SIZE);
		mailSession.setTempFile(sessionFile);		

		MailAccount mailAccount = new MailAccount();
		mailAccount.setId(accountId);
		
		Mockito.when(mailAccountDao.findByLoginAndDomain(Matchers.eq(recipient.getFirst()), Matchers.eq(recipient.getSecond()))).thenReturn(mailAccount);
		Mockito.when(mailFileProvider.getMessageFile(Matchers.eq(recipient), Matchers.anyInt())).thenReturn(targetFile);
		
		mailManager.saveMessage(mailSession);
		
		ArgumentCaptor<MailMessage> captor = ArgumentCaptor.forClass(MailMessage.class);
		Mockito.verify(mailMessageDao).save(captor.capture());
		MailMessage mailMessage = captor.getValue();
		assertEquals(accountId.intValue(), mailMessage.getAccountId());
		assertEquals(sender.joinString("@"), mailMessage.getSender());
		assertEquals(REAL_MAIL_SIZE, mailMessage.getSize());
		assertEquals(BODY_LINE, FileUtils.readFileToString(targetFile));

	}	
	
	
}
