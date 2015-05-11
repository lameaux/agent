package com.euromoby.cdn.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class CdnResourceTest {

	public static final String GOOD_URL_PREFIX = "/good_prefix";
	public static final String BAD_URL_PREFIX = "/bad_prefix";
	public static final String RESOURCE_ORIGIN = "http://example.com/assets";
	public static final String URL_PATTERN = ".*\\.jpg";
	public static final String GOOD_RESOURCE_NAME = "/good.jpg";
	public static final String BAD_RESOURCE_NAME = "/bad.png";	
	
	CdnResource cdnResource;

	@Before
	public void init() {
		cdnResource = new CdnResource();
		cdnResource.setUrlPathPrefix(GOOD_URL_PREFIX);
		cdnResource.setUrlPathPattern(URL_PATTERN);
		cdnResource.setResourceOrigin(RESOURCE_ORIGIN);
	}


	@Test
	public void testNotMatchingEmptyUrl() {
		assertFalse(cdnResource.matches(null));
	}
	
	@Test
	public void testMatchingAll() {
		// default - matches all
		cdnResource = new CdnResource();
		
		String goodUrl = GOOD_URL_PREFIX + GOOD_RESOURCE_NAME;
		assertTrue(cdnResource.matches(goodUrl));
		
		String badUrl = BAD_URL_PREFIX + BAD_RESOURCE_NAME;
		assertTrue(cdnResource.matches(badUrl));
		
		assertTrue(cdnResource.matches("foobar"));		
	}	
	
	@Test
	public void testMatchGoodUrl() {
		String goodUrl = GOOD_URL_PREFIX + GOOD_RESOURCE_NAME;
		assertTrue(cdnResource.matches(goodUrl));
	}	
	
	@Test
	public void testNotMatchingUrlPrefix() {
		String badUrl = BAD_URL_PREFIX + GOOD_RESOURCE_NAME;		
		assertFalse(cdnResource.matches(badUrl));
	}

	@Test
	public void testNotMatchingUrlPattern() {
		String badUrl = GOOD_URL_PREFIX + BAD_RESOURCE_NAME;		
		assertFalse(cdnResource.matches(badUrl));
	}	
	
	@Test
	public void testSourceUrl() {
		String goodUrl = GOOD_URL_PREFIX + GOOD_RESOURCE_NAME;
		assertEquals(RESOURCE_ORIGIN + GOOD_RESOURCE_NAME, cdnResource.getSourceUrl(goodUrl));
		
		cdnResource.setUrlPathPrefix(null);
		assertEquals(RESOURCE_ORIGIN + GOOD_URL_PREFIX + GOOD_RESOURCE_NAME, cdnResource.getSourceUrl(goodUrl));
		
	}

	@Test
	public void testNoSourceUrl() {
		assertNull(cdnResource.getSourceUrl(null));
		
		cdnResource.setResourceOrigin(null);
		String goodUrl = GOOD_URL_PREFIX + GOOD_RESOURCE_NAME;		
		assertNull(cdnResource.getSourceUrl(goodUrl));
	}	

	@Test
	public void testGetSet() {
		cdnResource = new CdnResource();
		cdnResource.setUrlPathPrefix(GOOD_URL_PREFIX);
		assertEquals(GOOD_URL_PREFIX, cdnResource.getUrlPathPrefix());
		cdnResource.setUrlPathPattern(URL_PATTERN);
		assertEquals(URL_PATTERN, cdnResource.getUrlPathPattern());
		cdnResource.setResourceOrigin(RESOURCE_ORIGIN);
		assertEquals(RESOURCE_ORIGIN, cdnResource.getResourceOrigin());		
		cdnResource.setDownloadIfMissing(true);
		assertTrue(cdnResource.isDownloadIfMissing());
		cdnResource.setProxyable(true);
		assertTrue(cdnResource.isProxyable());	
		cdnResource.setAvailableInNetwork(true);
		assertTrue(cdnResource.isAvailableInNetwork());			
	}
	
}
