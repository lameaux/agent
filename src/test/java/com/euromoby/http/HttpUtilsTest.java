package com.euromoby.http;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class HttpUtilsTest {

	@Test
	public void shouldBypassProxy() {
		String ADDRESS = "127.0.0.1";
		String HOST = "www.euromoby.com";

		String[] BYPASS_LIST = new String[] { "127.0.0.*", "localhost", "*.euromoby.*" };
		assertTrue(HttpUtils.bypassProxy(BYPASS_LIST, ADDRESS));
		assertTrue(HttpUtils.bypassProxy(BYPASS_LIST, HOST));
	}

	@Test
	public void shouldNotBypassProxy() {
		String ADDRESS = "127.0.1.1";
		String HOST = "euromoby.com";
		String[] BYPASS_LIST = new String[] { "127.0.0.*", "localhost", "*.euromoby.*" };
		assertFalse(HttpUtils.bypassProxy(BYPASS_LIST, ADDRESS));
		assertFalse(HttpUtils.bypassProxy(BYPASS_LIST, HOST));
	}

}
