package com.euromoby.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.atLeastOnce;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;

import java.io.File;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.euromoby.file.MimeHelper;
import com.euromoby.model.Tuple;
import com.euromoby.rest.ChunkedInputAdapter;
import com.euromoby.rest.RestException;

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
	@Mock
	ChannelFuture channelFuture;
	@Mock
	ChannelFuture lastContentFuture;
	@Mock
	RandomAccessFile raf;

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
		Mockito.when(mimeHelper.isBinary(file)).thenReturn(true);
		fileResponse.setHeaderContentDisposition(file);
		assertEquals(FileResponse.CONTENT_DISPOSITION_ATTACHMENT + ";filename=\"" + filename + "\"", fileResponse.getHeader(FileResponse.CONTENT_DISPOSITION));
		Mockito.when(mimeHelper.isBinary(file)).thenReturn(false);
		fileResponse.setHeaderContentDisposition(file);
		assertEquals(FileResponse.CONTENT_DISPOSITION_INLINE + ";filename=\"" + filename + "\"", fileResponse.getHeader(FileResponse.CONTENT_DISPOSITION));
	}

	@Test
	public void testSetHeaderContentType() {
		String contentType = "text/foo";
		Mockito.when(mimeHelper.getContentType(Matchers.eq(file))).thenReturn(contentType);
		fileResponse.setHeaderContentType(file);
		assertEquals(contentType + "; charset=UTF-8", fileResponse.getHeader(HttpHeaders.Names.CONTENT_TYPE));
	}
	
	@Test
	public void testSetHeaderContentEncodingCompressible() {
		Mockito.when(mimeHelper.isCompressible(file)).thenReturn(true);
		boolean ssl = false;
		fileResponse.setHeaderContentEncoding(file, ssl);
		assertNull(fileResponse.getHeader(HttpHeaders.Names.CONTENT_ENCODING));
	}

	@Test
	public void testSetHeaderContentEncodingNonCompressibleNoSsl() {
		Mockito.when(mimeHelper.isCompressible(file)).thenReturn(false);
		boolean ssl = false;
		fileResponse.setHeaderContentEncoding(file, ssl);
		assertEquals(HttpHeaders.Values.IDENTITY, fileResponse.getHeader(HttpHeaders.Names.CONTENT_ENCODING));
	}	

	@Test
	public void testSetHeaderContentEncodingNonCompressibleSsl() {
		Mockito.when(mimeHelper.isCompressible(file)).thenReturn(false);
		boolean ssl = true;
		fileResponse.setHeaderContentEncoding(file, ssl);
		assertEquals(HttpHeaders.Values.IDENTITY, fileResponse.getHeader(HttpHeaders.Names.CONTENT_ENCODING));
	}	

	@Test
	public void testSetHeaderContentEncodingCompressibleSsl() {
		Mockito.when(mimeHelper.isCompressible(file)).thenReturn(true);
		boolean ssl = true;
		fileResponse.setHeaderContentEncoding(file, ssl);
		assertEquals(HttpHeaders.Values.IDENTITY, fileResponse.getHeader(HttpHeaders.Names.CONTENT_ENCODING));
	}	

	@Test
	public void testSetHeaderConnectionKeepAlive() {
		Mockito.when(httpRequest.getProtocolVersion()).thenReturn(HttpVersion.HTTP_1_1);
		Mockito.when(requestHeaders.get(Matchers.eq(HttpHeaders.Names.CONNECTION))).thenReturn(HttpHeaders.Values.KEEP_ALIVE);
		fileResponse.setHeaderKeepAlive();
		assertEquals(HttpHeaders.Values.KEEP_ALIVE, fileResponse.getHeader(HttpHeaders.Names.CONNECTION));
	}
	
	@Test
	public void testSetHeaderConnectionClose() {
		Mockito.when(httpRequest.getProtocolVersion()).thenReturn(HttpVersion.HTTP_1_0);
		Mockito.when(requestHeaders.get(Matchers.eq(HttpHeaders.Names.CONNECTION))).thenReturn(HttpHeaders.Values.CLOSE);
		fileResponse.setHeaderKeepAlive();
		assertNull(fileResponse.getHeader(HttpHeaders.Names.CONNECTION));
	}	

	@Test
	public void testSetHeaderTransferEncodingChunked() {
		Mockito.when(httpRequest.getProtocolVersion()).thenReturn(HttpVersion.HTTP_1_1);
		fileResponse.setHeaderTransferEncoding();
		assertEquals(HttpHeaders.Values.CHUNKED, fileResponse.getHeader(HttpHeaders.Names.TRANSFER_ENCODING));
	}

	@Test
	public void testSetHeaderTransferEncodingNotChunked() {
		Mockito.when(httpRequest.getProtocolVersion()).thenReturn(HttpVersion.HTTP_1_0);
		fileResponse.setHeaderTransferEncoding();
		assertNull(fileResponse.getHeader(HttpHeaders.Names.TRANSFER_ENCODING));
	}	

	@Test
	public void testSupportChunks() {
		Mockito.when(httpRequest.getProtocolVersion()).thenReturn(HttpVersion.HTTP_1_0);
		assertFalse(fileResponse.supportChunks());
		Mockito.when(httpRequest.getProtocolVersion()).thenReturn(HttpVersion.HTTP_1_1);
		assertTrue(fileResponse.supportChunks());
	}
	
	@Test
	public void testSetHeaderContentLength() {
		long fileLength = 123;
		fileResponse.setHeaderContentLength(fileLength);
		assertEquals(String.valueOf(fileLength), fileResponse.getHeader(HttpHeaders.Names.CONTENT_LENGTH));		
	}
	
	@Test
	public void testSetHeaderContentRange() {
		long fileLength = 123;
		long rangeStart = 0;
		long rangeEnd = 100;
		Tuple<Long, Long> range = Tuple.of(rangeStart, rangeEnd);
		fileResponse.setHeaderContentRange(range, fileLength);	
		assertEquals("bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength, fileResponse.getHeader(HttpHeaders.Names.CONTENT_RANGE));			
	}
	
	@Test
	public void testSendInvalidFile() throws Exception {
		File file = new File("/fooo/bar/foo");
		try {
			fileResponse.send(ctx, file);
			fail();
		} catch (RestException e) {}
	}	


	@Test
	public void testSendInvalidRange() throws Exception {
		Mockito.when(httpRequest.getProtocolVersion()).thenReturn(HttpVersion.HTTP_1_0);
		Mockito.when(requestHeaders.get(Matchers.eq(HttpHeaders.Names.RANGE))).thenReturn("bytes=0-2000");
		try {
			fileResponse.send(ctx, file);
			fail();
		} catch (RestException e) {}
	}	
	
	@Test
	public void testSendFileBodySsl() throws Exception {
		long offset = 56;
		long length = 123;
		Mockito.when(channelPipeline.get(Matchers.eq(SslHandler.class))).thenReturn(Mockito.mock(SslHandler.class));
		fileResponse.sendFileBody(ctx, raf, offset, length);
		ArgumentCaptor<ChunkedFile> responseCaptor = ArgumentCaptor.forClass(ChunkedFile.class);
		Mockito.verify(ctx).write(responseCaptor.capture());
		ChunkedFile chunkedFile = responseCaptor.getValue();
		assertEquals(offset, chunkedFile.startOffset());
		assertEquals(offset + length, chunkedFile.endOffset());
	}

	@Test
	public void testSendFileBodyChunked() throws Exception {
		long offset = 56;
		long length = 123;
		Mockito.when(httpRequest.getProtocolVersion()).thenReturn(HttpVersion.HTTP_1_1);		
		fileResponse.sendFileBody(ctx, raf, offset, length);
		Mockito.verify(ctx).write(Matchers.any(ChunkedInputAdapter.class));
	}	

	@Test
	public void testSendFileBodyNotChunked() throws Exception {
		long offset = 56;
		long length = 123;
		Mockito.when(httpRequest.getProtocolVersion()).thenReturn(HttpVersion.HTTP_1_0);
		File tmpFile = File.createTempFile("foo", "bar");
		tmpFile.deleteOnExit();
		RandomAccessFile tempRaf = new RandomAccessFile(tmpFile, "r");
		fileResponse.sendFileBody(ctx, tempRaf, offset, length);
		ArgumentCaptor<DefaultFileRegion> responseCaptor = ArgumentCaptor.forClass(DefaultFileRegion.class);
		Mockito.verify(ctx).write(responseCaptor.capture());
		DefaultFileRegion defaultFileRegion = responseCaptor.getValue();
		assertEquals(offset, defaultFileRegion.position());
		assertEquals(length, defaultFileRegion.count());
	}	
	
	@Test
	public void testSendFileContentWithKeepAlive() throws Exception {
		long offset = 56;
		long length = 123;
		File tmpFile = File.createTempFile("foo", "bar");
		tmpFile.deleteOnExit();
		Mockito.when(channelPipeline.get(Matchers.eq(SslHandler.class))).thenReturn(Mockito.mock(SslHandler.class));		
		Mockito.when(httpRequest.getProtocolVersion()).thenReturn(HttpVersion.HTTP_1_1);
		Mockito.when(requestHeaders.get(Matchers.eq(HttpHeaders.Names.CONNECTION))).thenReturn(HttpHeaders.Values.KEEP_ALIVE);
		Mockito.when(ctx.writeAndFlush(Matchers.eq(DefaultLastHttpContent.EMPTY_LAST_CONTENT))).thenReturn(lastContentFuture);
		fileResponse.sendFileContent(ctx, tmpFile, offset, length);
		ArgumentCaptor<ChannelFutureListener> responseCaptor = ArgumentCaptor.forClass(ChannelFutureListener.class);
		Mockito.verify(lastContentFuture).addListener(responseCaptor.capture());
		ChannelFutureListener channelFutureListener = responseCaptor.getValue();
		channelFutureListener.operationComplete(lastContentFuture);
	}

	@Test
	public void testSendFileContentWithConnectionClose() throws Exception {
		long offset = 56;
		long length = 123;
		File tmpFile = File.createTempFile("foo", "bar");
		tmpFile.deleteOnExit();
		Mockito.when(channelPipeline.get(Matchers.eq(SslHandler.class))).thenReturn(Mockito.mock(SslHandler.class));		
		Mockito.when(httpRequest.getProtocolVersion()).thenReturn(HttpVersion.HTTP_1_0);
		Mockito.when(requestHeaders.get(Matchers.eq(HttpHeaders.Names.CONNECTION))).thenReturn(HttpHeaders.Values.CLOSE);
		Mockito.when(ctx.writeAndFlush(Matchers.eq(DefaultLastHttpContent.EMPTY_LAST_CONTENT))).thenReturn(lastContentFuture);
		fileResponse.sendFileContent(ctx, tmpFile, offset, length);
		Mockito.verify(lastContentFuture).addListener(Matchers.eq(ChannelFutureListener.CLOSE));		
	}
	
	
	//@Test
	public void testSendChunkedNotSsl() throws Exception {
		File tempFile = File.createTempFile("prefix", "suffix");
		tempFile.deleteOnExit();
		
		Mockito.when(httpRequest.getProtocolVersion()).thenReturn(HttpVersion.HTTP_1_1);

		Mockito.when(ctx.writeAndFlush(Matchers.eq(DefaultLastHttpContent.EMPTY_LAST_CONTENT))).thenReturn(lastContentFuture);
		fileResponse.send(ctx, tempFile);

		ArgumentCaptor<Object> responseCaptor = ArgumentCaptor.forClass(Object.class);
		Mockito.verify(ctx, atLeastOnce()).write(responseCaptor.capture());
		Mockito.verify(ctx, atLeastOnce()).writeAndFlush(Matchers.eq(DefaultLastHttpContent.EMPTY_LAST_CONTENT));

		List<Object> arguments = responseCaptor.getAllValues();
		DefaultHttpResponse response = null;
		ChunkedInputAdapter chunkedInputAdapter = null;
		for (Object argument : arguments) {
			if (argument instanceof DefaultHttpResponse) {
				response = (DefaultHttpResponse) argument;
				continue;
			}
			if (argument instanceof ChunkedInputAdapter) {
				chunkedInputAdapter = (ChunkedInputAdapter) argument;
				continue;
			}
			fail("Invalid argument " + argument.getClass().getName());
		}
		assertNotNull(response);
		assertNotNull(chunkedInputAdapter);
		assertEquals(String.valueOf(tempFile.length()), response.headers().get(HttpHeaders.Names.CONTENT_LENGTH));
		assertEquals(HttpHeaders.Values.BYTES, response.headers().get(HttpHeaders.Names.ACCEPT_RANGES));
		assertEquals(HttpHeaders.Values.CHUNKED, response.headers().get(HttpHeaders.Names.TRANSFER_ENCODING));
		assertEquals(HttpResponseStatus.OK, response.getStatus());
	}

	//@Test
	public void testSendWithSsl() throws Exception {
		File tempFile = File.createTempFile("prefix", "suffix");
		tempFile.deleteOnExit();
		Mockito.when(channelPipeline.get(Matchers.eq(SslHandler.class))).thenReturn(Mockito.mock(SslHandler.class));
		Mockito.when(httpRequest.getProtocolVersion()).thenReturn(HttpVersion.HTTP_1_1);
		Mockito.when(ctx.writeAndFlush(Matchers.eq(DefaultLastHttpContent.EMPTY_LAST_CONTENT))).thenReturn(lastContentFuture);
		fileResponse.send(ctx, tempFile);
		
		ArgumentCaptor<Object> responseCaptor = ArgumentCaptor.forClass(Object.class);
		Mockito.verify(ctx, atLeastOnce()).write(responseCaptor.capture());
		Mockito.verify(ctx, atLeastOnce()).writeAndFlush(Matchers.eq(DefaultLastHttpContent.EMPTY_LAST_CONTENT));

		List<Object> arguments = responseCaptor.getAllValues();
		DefaultHttpResponse response = null;
		ChunkedFile chunkedFile = null;
		for (Object argument : arguments) {
			if (argument instanceof DefaultHttpResponse) {
				response = (DefaultHttpResponse) argument;
				continue;
			}
			if (argument instanceof ChunkedFile) {
				chunkedFile = (ChunkedFile) argument;
				continue;
			}
			fail("Invalid argument " + argument.getClass().getName());
		}
		assertNotNull(response);
		assertNotNull(chunkedFile);
		assertEquals(HttpHeaders.Values.IDENTITY, response.headers().get(HttpHeaders.Names.CONTENT_ENCODING));		
		assertEquals(HttpResponseStatus.OK, response.getStatus());	
	}	

	//@Test
	public void testSendNoChunksNoSsl() throws Exception {
		File tempFile = File.createTempFile("prefix", "suffix");
		tempFile.deleteOnExit();
		Mockito.when(httpRequest.getProtocolVersion()).thenReturn(HttpVersion.HTTP_1_0);
		Mockito.when(ctx.writeAndFlush(Matchers.eq(DefaultLastHttpContent.EMPTY_LAST_CONTENT))).thenReturn(lastContentFuture);
		fileResponse.send(ctx, tempFile);
		
		ArgumentCaptor<Object> responseCaptor = ArgumentCaptor.forClass(Object.class);
		Mockito.verify(ctx, atLeastOnce()).write(responseCaptor.capture());
		Mockito.verify(ctx, atLeastOnce()).writeAndFlush(Matchers.eq(DefaultLastHttpContent.EMPTY_LAST_CONTENT));

		List<Object> arguments = responseCaptor.getAllValues();
		DefaultHttpResponse response = null;
		DefaultFileRegion defaultFileRegion = null;
		for (Object argument : arguments) {
			if (argument instanceof DefaultHttpResponse) {
				response = (DefaultHttpResponse) argument;
				continue;
			}
			if (argument instanceof DefaultFileRegion) {
				defaultFileRegion = (DefaultFileRegion) argument;
				continue;
			}
			fail("Invalid argument " + argument.getClass().getName());
		}
		assertNotNull(response);
		assertNotNull(defaultFileRegion);
		assertNull(response.headers().get(HttpHeaders.Names.TRANSFER_ENCODING));
		assertEquals(HttpResponseStatus.OK, response.getStatus());		
	}	
	
	
	//@Test
	public void testSendRangesChunkedNoSsl() throws Exception {
		File tempFile = File.createTempFile("prefix", "suffix");
		FileUtils.write(tempFile, "fooooo");
		tempFile.deleteOnExit();
		Mockito.when(httpRequest.getProtocolVersion()).thenReturn(HttpVersion.HTTP_1_1);
		Mockito.when(requestHeaders.get(Matchers.eq(HttpHeaders.Names.RANGE))).thenReturn("bytes=0-5");
		Mockito.when(ctx.writeAndFlush(Matchers.eq(DefaultLastHttpContent.EMPTY_LAST_CONTENT))).thenReturn(lastContentFuture);
		fileResponse.send(ctx, tempFile);

		ArgumentCaptor<Object> responseCaptor = ArgumentCaptor.forClass(Object.class);
		Mockito.verify(ctx, atLeastOnce()).write(responseCaptor.capture());
		Mockito.verify(ctx, atLeastOnce()).writeAndFlush(Matchers.eq(DefaultLastHttpContent.EMPTY_LAST_CONTENT));

		List<Object> arguments = responseCaptor.getAllValues();
		DefaultHttpResponse response = null;
		ChunkedInputAdapter chunkedInputAdapter = null;
		for (Object argument : arguments) {
			if (argument instanceof DefaultHttpResponse) {
				response = (DefaultHttpResponse) argument;
				continue;
			}
			if (argument instanceof ChunkedInputAdapter) {
				chunkedInputAdapter = (ChunkedInputAdapter) argument;
				continue;
			}
			fail("Invalid argument " + argument.getClass().getName());
		}
		assertNotNull(response);
		assertNotNull(chunkedInputAdapter);
		assertEquals(String.valueOf(tempFile.length()), response.headers().get(HttpHeaders.Names.CONTENT_LENGTH));
		assertEquals(HttpHeaders.Values.BYTES, response.headers().get(HttpHeaders.Names.ACCEPT_RANGES));
		assertEquals(HttpHeaders.Values.CHUNKED, response.headers().get(HttpHeaders.Names.TRANSFER_ENCODING));
		assertNotNull(response.headers().get(HttpHeaders.Names.CONTENT_RANGE));
		assertEquals(HttpResponseStatus.PARTIAL_CONTENT, response.getStatus());
	}
	
}
