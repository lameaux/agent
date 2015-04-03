package com.euromoby.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.euromoby.utils.DateUtils;

public class TimeCommandTest {

	TimeCommand timeCommand;

	@Before
	public void init() {
		timeCommand = new TimeCommand();
	}

	@Test
	public void testMatchName() {
		assertTrue(timeCommand.match(TimeCommand.NAME));
	}
	
	@Test
	public void testNotMatch() {
		assertFalse(timeCommand.match(TimeCommand.NAME + "aaa"));
	}

	@Test
	public void testDefault() {
		String result = timeCommand.execute(timeCommand.name());
		assertEquals(DateUtils.iso(System.currentTimeMillis()), result);
	}
	
}
