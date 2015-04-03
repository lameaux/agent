package com.euromoby.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.euromoby.utils.NetUtils;
import com.euromoby.utils.StringUtils;

public class InetAddrCommandTest {

	InetAddrCommand inetAddrCommand;

	@Before
	public void init() {
		inetAddrCommand = new InetAddrCommand();
	}

	@Test
	public void testMatchName() {
		assertTrue(inetAddrCommand.match(InetAddrCommand.NAME));
	}
	
	@Test
	public void testNotMatch() {
		assertFalse(inetAddrCommand.match(InetAddrCommand.NAME + "aaa"));
	}

	@Test
	public void testDefault() {
		StringBuffer sb = new StringBuffer();
		sb.append(NetUtils.getHostname()).append(StringUtils.CRLF);
		sb.append(Arrays.toString(NetUtils.getAllInterfaces().toArray()));
		
		String result = inetAddrCommand.execute(inetAddrCommand.name());
		assertEquals(sb.toString(), result);
	}

	
}
