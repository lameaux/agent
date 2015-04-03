package com.euromoby.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.euromoby.utils.StringUtils;

public class SysInfoCommandTest {

	SysInfoCommand sysinfo;

	@Before
	public void init() {
		sysinfo = new SysInfoCommand();
	}

	@Test
	public void testMatchName() {
		assertTrue(sysinfo.match(SysInfoCommand.NAME));
	}

	@Test
	public void testMatchWithParam() {
		assertTrue(sysinfo.match(SysInfoCommand.NAME + Command.SEPARATOR + "param"));
	}
	
	@Test
	public void testNotMatch() {
		assertFalse(sysinfo.match(SysInfoCommand.NAME + "aaa"));
	}	
	
	@Test
	public void testDefault() {
		String response = StringUtils.printProperties(System.getProperties(), null);
		assertEquals(response, sysinfo.execute(sysinfo.name()));
	}	
	
	@Test
	public void testParameter() {
		String parameter = "user.home";
		String response = StringUtils.printProperties(System.getProperties(), parameter);
		assertEquals(response, sysinfo.execute(sysinfo.name() + Command.SEPARATOR + parameter));
	}	
	
}
