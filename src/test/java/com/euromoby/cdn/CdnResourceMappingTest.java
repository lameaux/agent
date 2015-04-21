package com.euromoby.cdn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class CdnResourceMappingTest {

	public static final String GOOD_URL_PREFIX = "/good_prefix";
	public static final String BAD_URL_PREFIX = "/bad_prefix";
	public static final String RESOURCE_ORIGIN = "http://example.com/assets";
	public static final String URL_PATTERN = ".*\\.jpg";
	public static final String GOOD_RESOURCE_NAME = "/good.jpg";
	public static final String BAD_RESOURCE_NAME = "/bad.png";	
	
	CdnResourceMapping cdnResourceMapping;

	@Before
	public void init() {
		cdnResourceMapping = new CdnResourceMapping();
		cdnResourceMapping.setUrlPrefix(GOOD_URL_PREFIX);
		cdnResourceMapping.setUrlPattern(URL_PATTERN);
		cdnResourceMapping.setResourceOrigin(RESOURCE_ORIGIN);
	}


	@Test
	public void testNotMatchingEmptyUrl() {
		assertFalse(cdnResourceMapping.matches(null));
	}
	
	@Test
	public void testMatchingAll() {
		// default - matches all
		cdnResourceMapping = new CdnResourceMapping();
		
		String goodUrl = GOOD_URL_PREFIX + GOOD_RESOURCE_NAME;
		assertTrue(cdnResourceMapping.matches(goodUrl));
		
		String badUrl = BAD_URL_PREFIX + BAD_RESOURCE_NAME;
		assertTrue(cdnResourceMapping.matches(badUrl));
		
		assertTrue(cdnResourceMapping.matches("foobar"));		
	}	
	
	@Test
	public void testMatchGoodUrl() {
		String goodUrl = GOOD_URL_PREFIX + GOOD_RESOURCE_NAME;
		assertTrue(cdnResourceMapping.matches(goodUrl));
	}	
	
	@Test
	public void testNotMatchingUrlPrefix() {
		String badUrl = BAD_URL_PREFIX + GOOD_RESOURCE_NAME;		
		assertFalse(cdnResourceMapping.matches(badUrl));
	}

	@Test
	public void testNotMatchingUrlPattern() {
		String badUrl = GOOD_URL_PREFIX + BAD_RESOURCE_NAME;		
		assertFalse(cdnResourceMapping.matches(badUrl));
	}	
	
	@Test
	public void testSourceUrl() {
		String goodUrl = GOOD_URL_PREFIX + GOOD_RESOURCE_NAME;
		assertEquals(RESOURCE_ORIGIN + GOOD_RESOURCE_NAME, cdnResourceMapping.getSourceUrl(goodUrl));
		
		cdnResourceMapping.setUrlPrefix(null);
		assertEquals(RESOURCE_ORIGIN + GOOD_URL_PREFIX + GOOD_RESOURCE_NAME, cdnResourceMapping.getSourceUrl(goodUrl));
		
	}

	@Test
	public void testNoSourceUrl() {
		assertNull(cdnResourceMapping.getSourceUrl(null));
		
		cdnResourceMapping.setResourceOrigin(null);
		String goodUrl = GOOD_URL_PREFIX + GOOD_RESOURCE_NAME;		
		assertNull(cdnResourceMapping.getSourceUrl(goodUrl));
	}	

	@Test
	public void testGetSet() {
		cdnResourceMapping = new CdnResourceMapping();
		cdnResourceMapping.setUrlPrefix(GOOD_URL_PREFIX);
		assertEquals(GOOD_URL_PREFIX, cdnResourceMapping.getUrlPrefix());
		cdnResourceMapping.setUrlPattern(URL_PATTERN);
		assertEquals(URL_PATTERN, cdnResourceMapping.getUrlPattern());
		cdnResourceMapping.setResourceOrigin(RESOURCE_ORIGIN);
		assertEquals(RESOURCE_ORIGIN, cdnResourceMapping.getResourceOrigin());		
		cdnResourceMapping.setDownloadIfMissing(true);
		assertTrue(cdnResourceMapping.isDownloadIfMissing());
		cdnResourceMapping.setStreamable(true);
		assertTrue(cdnResourceMapping.isStreamable());		
	}
	
}
