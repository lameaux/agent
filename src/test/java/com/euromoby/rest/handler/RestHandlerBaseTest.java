package com.euromoby.rest.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.ClientCookieEncoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RestHandlerBaseTest {

	@Mock
	HttpRequest request;
	@Mock
	HttpHeaders headers;
	@Mock
	ChannelHandlerContext ctx;
	@Mock
	Channel channel;
	@Mock
	ChannelFuture future;
	@Mock 
	File fileToDelete;
	
	RestHandlerBase restHandlerBase;
	
	@Before
	public void init() {
		
		Mockito.when(request.getProtocolVersion()).thenReturn(HttpVersion.HTTP_1_1);
		Mockito.when(request.headers()).thenReturn(headers);
		
		Mockito.when(ctx.channel()).thenReturn(channel);
		Mockito.when(channel.writeAndFlush(Matchers.any(FullHttpResponse.class))).thenReturn(future);
		
		
		restHandlerBase = new RestHandlerBase() {
			@Override
			public boolean matchUri(URI uri) {
				return true;
			}
		}; 
	}
	
	@Test
	public void testDoGet() throws Exception {
		FullHttpResponse response = restHandlerBase.doGet(ctx, request, null);
		assertEquals(HttpResponseStatus.NOT_IMPLEMENTED, response.getStatus());
	}

	@Test
	public void testDoPost() throws Exception {
		FullHttpResponse response = restHandlerBase.doPost(ctx, request, null, null, null);
		assertEquals(HttpResponseStatus.NOT_IMPLEMENTED, response.getStatus());
	}

	@Test
	public void testDoGetChunked() throws Exception {
		restHandlerBase.doGetChunked(ctx, request, null);
		ArgumentCaptor<FullHttpResponse> argumentCaptor = ArgumentCaptor.forClass(FullHttpResponse.class); 
		Mockito.verify(channel).writeAndFlush(argumentCaptor.capture());
		FullHttpResponse response = argumentCaptor.getValue();
		assertEquals(HttpResponseStatus.NOT_IMPLEMENTED, response.getStatus());
	}
	
	
	@Test
	public void shouldBeNotChunked() {
		assertFalse(restHandlerBase.isChunkedResponse());
	}
	
	@Test
	public void testGetCookies() {
		Cookie empty = restHandlerBase.getCookie(request);
		assertNull(empty);
		
		Set<Cookie> cookies = new HashSet<Cookie>();
		String COOKIE_NAME = "foo";
		String COOKIE_VALUE = "bar";
		Cookie cookie1 = new DefaultCookie(COOKIE_NAME, COOKIE_VALUE);
		cookies.add(cookie1);
		String ENCODED_COOKIES = ClientCookieEncoder.LAX.encode(cookies);
		
		Mockito.when(headers.get(Matchers.eq(HttpHeaders.Names.COOKIE))).thenReturn(ENCODED_COOKIES);
		
		Cookie headerCookie = restHandlerBase.getCookie(request);
		assertNotNull(headerCookie);
		assertEquals(COOKIE_NAME, headerCookie.name());
		assertEquals(COOKIE_VALUE, headerCookie.value());		
	}
	
	@Test
	public void testGetUriAttributesEmpty() {
		Mockito.when(request.getUri()).thenReturn("foobar");
		Map<String, List<String>> attrs = restHandlerBase.getUriAttributes(request);
		assertTrue(attrs.isEmpty());
	}

	@Test
	public void testGetUriAttributesNotEmpty() {
		Mockito.when(request.getUri()).thenReturn("/?foo=bar");
		Map<String, List<String>> attrs = restHandlerBase.getUriAttributes(request);
		assertFalse(attrs.isEmpty());
		List<String> fooAttr = attrs.get("foo");
		assertEquals("bar", fooAttr.iterator().next());
	}
	
	@Test
	public void testDeleteTempFiles() {
		Map<String, File> requestFiles = new HashMap<String, File>();
		requestFiles.put("foo", fileToDelete);
		restHandlerBase.deleteTempFiles(requestFiles);
		Mockito.verify(fileToDelete).delete();
		
	}
	
}
