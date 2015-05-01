package com.euromoby.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import com.euromoby.utils.StringUtils;

public class MailSessionTest {

	MailSession mailSession;

	@Before
	public void init() {
		mailSession = new MailSession();
	}

	@Test
	public void isEndOfTransfer() {
		assertTrue(mailSession.isEndOfTransfer("."));
		assertFalse(mailSession.isEndOfTransfer(""));
	}

	@Test
	public void shouldResetSession() {
		mailSession.reset();
		assertTrue(mailSession.isCommandMode());
		assertNull(mailSession.getDomain());
		assertTrue(mailSession.getSender().isEmpty());
		assertTrue(mailSession.getRecipient().isEmpty());
		assertEquals(0, mailSession.getDeclaredMailSize());
		assertEquals(0, mailSession.getRealMailSize());
		assertTrue(mailSession.isProcessingHeaders());
		assertTrue(mailSession.getHeaders().isEmpty());
		assertNull(mailSession.getTempFile());
	}

	@Test
	public void shouldThrowMailSizeException() throws Exception {
		mailSession.setRealMailSize(MailSession.MAX_MESSAGE_SIZE);
		try {
			mailSession.processDataLine("foo");
			fail();
		} catch (MailSizeException e) {}
	}
	
	@Test
	public void testReceiveMail() throws Exception {
		
		File tempFile = File.createTempFile("foo", "bar");
		tempFile.deleteOnExit();
		mailSession.setTempFile(tempFile);
		String fileContent = "";
		
		String HEADER_SUBJECT = "Subject: hello";
		assertTrue(mailSession.isProcessingHeaders());
		assertFalse(mailSession.processDataLine(HEADER_SUBJECT));
		fileContent += HEADER_SUBJECT + StringUtils.CRLF;
		assertEquals(fileContent, FileUtils.readFileToString(tempFile));
		assertTrue(mailSession.getHeaders().contains(HEADER_SUBJECT));
		assertFalse(mailSession.processDataLine(""));
		assertFalse(mailSession.isProcessingHeaders());
		fileContent += StringUtils.CRLF;
		assertEquals(fileContent, FileUtils.readFileToString(tempFile));
		
		String BODY_LINE = "Hello World!";
		assertFalse(mailSession.processDataLine(BODY_LINE));
		fileContent += BODY_LINE + StringUtils.CRLF;
		assertEquals(fileContent, FileUtils.readFileToString(tempFile));		
		assertFalse(mailSession.processDataLine(""));
		fileContent += StringUtils.CRLF;
		assertEquals(fileContent, FileUtils.readFileToString(tempFile));		
		assertTrue(mailSession.processDataLine("."));
		assertEquals(fileContent, FileUtils.readFileToString(tempFile));		
		
	}

}
