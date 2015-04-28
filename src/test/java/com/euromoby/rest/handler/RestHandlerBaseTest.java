package com.euromoby.rest.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.ClientCookieEncoder;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.DefaultCookie;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.io.File;
import java.net.InetAddress;
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
		restHandlerBase.setHttpRequest(request);
		
	}
	
	@Test
	public void testDoGet() throws Exception {
		FullHttpResponse response = restHandlerBase.doGet();
		assertEquals(HttpResponseStatus.NOT_IMPLEMENTED, response.getStatus());
	}

	@Test
	public void testDoPost() throws Exception {
		FullHttpResponse response = restHandlerBase.doPost();
		assertEquals(HttpResponseStatus.NOT_IMPLEMENTED, response.getStatus());
	}

	@Test
	public void testDoGetChunked() throws Exception {
		restHandlerBase.doGetChunked(ctx);
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
	public void testGetHeaders() throws Exception {
		assertEquals(headers, restHandlerBase.getHeaders());
	}	
	
	@Test
	public void testGetCookies() {
		Set<Cookie> empty = restHandlerBase.getCookies();
		assertTrue(empty.isEmpty());
		
		Set<Cookie> cookies = new HashSet<Cookie>();
		String COOKIE_NAME = "foo";
		String COOKIE_VALUE = "bar";
		Cookie cookie1 = new DefaultCookie(COOKIE_NAME, COOKIE_VALUE);
		cookies.add(cookie1);
		String ENCODED_COOKIES = ClientCookieEncoder.encode(cookies);
		
		Mockito.when(headers.get(Matchers.eq(HttpHeaders.Names.COOKIE))).thenReturn(ENCODED_COOKIES);
		
		Set<Cookie> headerCookies = restHandlerBase.getCookies();
		assertFalse(headerCookies.isEmpty());
		
		Cookie headerCookie = headerCookies.iterator().next();
		assertEquals(COOKIE_NAME, headerCookie.getName());
		assertEquals(COOKIE_VALUE, headerCookie.getValue());		
	}
	
	@Test
	public void testGetClientInetAddress() throws Exception {
		restHandlerBase.setClientInetAddress(InetAddress.getLocalHost());
		assertEquals(InetAddress.getLocalHost(), restHandlerBase.getClientInetAddress());
	}
	
	@Test
	public void testGetUriAttributesEmpty() {
		Mockito.when(request.getUri()).thenReturn("foobar");
		Map<String, List<String>> attrs = restHandlerBase.getUriAttributes();
		assertTrue(attrs.isEmpty());
	}

	@Test
	public void testGetUriAttributesNotEmpty() {
		Mockito.when(request.getUri()).thenReturn("/?foo=bar");
		Map<String, List<String>> attrs = restHandlerBase.getUriAttributes();
		assertFalse(attrs.isEmpty());
		List<String> fooAttr = attrs.get("foo");
		assertEquals("bar", fooAttr.iterator().next());
	}
	
	@Test
	public void testDeleteTempFiles() {
		Map<String, File> requestFiles = new HashMap<String, File>();
		requestFiles.put("foo", fileToDelete);
		restHandlerBase.setRequestFiles(requestFiles);
		restHandlerBase.deleteTempFiles();
		Mockito.verify(fileToDelete).delete();
		
	}
	
}
