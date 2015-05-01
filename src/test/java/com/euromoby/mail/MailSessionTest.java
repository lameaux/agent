package com.euromoby.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

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
		assertTrue(mailSession.getBody().isEmpty());
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
		String HEADER_SUBJECT = "Subject: hello";
		assertTrue(mailSession.isProcessingHeaders());
		assertFalse(mailSession.processDataLine(HEADER_SUBJECT));
		assertTrue(mailSession.getHeaders().contains(HEADER_SUBJECT));
		assertFalse(mailSession.processDataLine(""));
		assertFalse(mailSession.isProcessingHeaders());
		
		String BODY_LINE = "Hello World!";
		assertFalse(mailSession.processDataLine(BODY_LINE));
		assertTrue(mailSession.getBody().contains(BODY_LINE));
		assertFalse(mailSession.processDataLine(""));
		assertFalse(mailSession.isProcessingHeaders());
		assertTrue(mailSession.getBody().contains(""));
		
		assertTrue(mailSession.processDataLine("."));
		
	}

}
