package com.euromoby.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.ssl.SslHandler;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.euromoby.file.MimeHelper;
import com.euromoby.model.Tuple;

@RunWith(MockitoJUnitRunner.class)
public class FileResponseTest {

	FileResponse fileResponse;
	@Mock
	HttpRequest httpRequest;
	@Mock
	HttpHeaders requestHeaders;
	@Mock
	MimeHelper mimeHelper;
	@Mock
	ChannelHandlerContext ctx;
	@Mock
	Channel channel;
	@Mock
	ChannelPipeline channelPipeline;
	@Mock
	File file;
	@Mock
	HttpResponse httpResponse;
	@Mock
	HttpHeaders responseHeaders;

	@Before
	public void init() {
		Mockito.when(ctx.channel()).thenReturn(channel);
		Mockito.when(channel.pipeline()).thenReturn(channelPipeline);
		Mockito.when(httpRequest.headers()).thenReturn(requestHeaders);
		Mockito.when(httpResponse.headers()).thenReturn(responseHeaders);
		fileResponse = new FileResponse(httpRequest, mimeHelper);
	}

	@Test
	public void testIsSsl() {
		Mockito.when(channelPipeline.get(Matchers.eq(SslHandler.class))).thenReturn(Mockito.mock(SslHandler.class));
		assertTrue(fileResponse.isSSL(ctx));
		Mockito.when(channelPipeline.get(Matchers.eq(SslHandler.class))).thenReturn(null);
		assertFalse(fileResponse.isSSL(ctx));
	}

	@Test
	public void testSetHeader() {
		String NAME = "foo";
		String VALUE = "bar";
		fileResponse.setHeader(NAME, VALUE);
		assertEquals(VALUE, fileResponse.getHeader(NAME));
	}

	@Test
	public void testSetupResponseHeaders() {
		String NAME = "foo";
		String VALUE = "bar";
		fileResponse.setHeader(NAME, VALUE);

		fileResponse.setupResponseHeaders(httpResponse);
		Mockito.verify(responseHeaders).set(Matchers.eq(NAME), Matchers.eq(VALUE));
	}

	@Test
	public void testSetDateAndCacheHeaders() {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(HttpUtils.HTTP_DATE_FORMAT, Locale.US);
		dateFormatter.setTimeZone(TimeZone.getTimeZone(HttpUtils.HTTP_DATE_GMT_TIMEZONE));

		long lastModified = System.currentTimeMillis();
		Mockito.when(file.lastModified()).thenReturn(lastModified);
		fileResponse.setDateAndCacheHeaders(file);

		assertNotNull(fileResponse.getHeader(HttpHeaders.Names.DATE));
		assertNotNull(fileResponse.getHeader(HttpHeaders.Names.EXPIRES));
		assertEquals(FileResponse.MAX_AGE_VALUE + HttpUtils.HTTP_CACHE_SECONDS, fileResponse.getHeader(HttpHeaders.Names.CACHE_CONTROL));
		assertEquals(dateFormatter.format(new Date(lastModified)), fileResponse.getHeader(HttpHeaders.Names.LAST_MODIFIED));
	}

	@Test
	public void testNoRangeHeader() {
		assertNull(fileResponse.parseRange(0));
	}

	@Test
	public void testEmptyRangeHeader() {
		Mockito.when(requestHeaders.get(Matchers.eq(HttpHeaders.Names.RANGE))).thenReturn("");
		assertNull(fileResponse.parseRange(0));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testInvalidPrefixRangeHeader() {
		Mockito.when(requestHeaders.get(Matchers.eq(HttpHeaders.Names.RANGE))).thenReturn("foo");
		fileResponse.parseRange(0);
		fail();
	}

	@Test
	public void testRightBeforeLeftRangeHeader() {
		Mockito.when(requestHeaders.get(Matchers.eq(HttpHeaders.Names.RANGE))).thenReturn("bytes=5-1");
		assertNull(fileResponse.parseRange(10));
	}	

	@Test(expected=IllegalArgumentException.class)
	public void testRightBiggerThanLengthRangeHeader() {
		Mockito.when(requestHeaders.get(Matchers.eq(HttpHeaders.Names.RANGE))).thenReturn("bytes=0-20");
		fileResponse.parseRange(10);
		fail();
	}		
	
	@Test
	public void testLeftRangeHeader() {
		long leftRange = 5;
		Mockito.when(requestHeaders.get(Matchers.eq(HttpHeaders.Names.RANGE))).thenReturn("bytes=" + leftRange + "-");
		long fileSize = 25;
		Tuple<Long, Long> range = fileResponse.parseRange(fileSize);
		assertEquals(leftRange, range.getFirst().longValue());
		assertEquals(fileSize-1, range.getSecond().longValue());		
	}
	
	@Test
	public void testBothRangeHeader() {
		long leftRange = 5;
		long rightRange = 15;		
		Mockito.when(requestHeaders.get(Matchers.eq(HttpHeaders.Names.RANGE))).thenReturn("bytes=" + leftRange + "-" + rightRange);
		long fileSize = 25;
		Tuple<Long, Long> range = fileResponse.parseRange(fileSize);
		assertEquals(leftRange, range.getFirst().longValue());
		assertEquals(rightRange, range.getSecond().longValue());		
	}	

	
	@Test
	public void testSetHeaderContentDisposition() {
		String filename = "foo";
		Mockito.when(file.getName()).thenReturn(filename);
		fileResponse.setHeaderContentDisposition(file, true);
		assertEquals(FileResponse.CONTENT_DISPOSITION_ATTACHMENT + ";filename=\"" + filename + "\"", fileResponse.getHeader(FileResponse.CONTENT_DISPOSITION));
		fileResponse.setHeaderContentDisposition(file, false);
		assertEquals(FileResponse.CONTENT_DISPOSITION_INLINE + ";filename=\"" + filename + "\"", fileResponse.getHeader(FileResponse.CONTENT_DISPOSITION));
	}

	@Test
	public void testSetHeaderContentType() {
		String contentType = "text/foo";
		Mockito.when(mimeHelper.getContentType(Matchers.eq(file))).thenReturn(contentType);
		fileResponse.setHeaderContentType(file);
		assertEquals(contentType + "; charset=UTF-8", fileResponse.getHeader(HttpHeaders.Names.CONTENT_TYPE));
	}
	
}
