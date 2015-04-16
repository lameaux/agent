package com.euromoby.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.io.File;
import java.net.URI;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.euromoby.cdn.CdnNetwork;
import com.euromoby.file.MimeHelper;
import com.euromoby.rest.handler.RestHandler;

@RunWith(MockitoJUnitRunner.class)
public class RestServerHandlerTest {

	private static final String INVALID_URI = "$[level]/r$[y]_c$[x].jpg";

	@Mock
	RestMapper restMapper;
	@Mock
	RestHandler restHandler;
	
	@Mock
	MimeHelper mimeHelper;
	@Mock
	CdnNetwork cdnNetwork;
	@Mock
	ChannelHandlerContext ctx;
	@Mock
	Channel channel;
	@Mock
	FullHttpRequest request;
	@Mock
	HttpHeaders headers;
	@Mock
	ChannelFuture channelFuture;
	@Mock
	File targetFile;

	RestServerHandler handler;

	@Before
	public void init() {
		Mockito.when(ctx.channel()).thenReturn(channel);
		Mockito.when(request.headers()).thenReturn(headers);
		Mockito.when(request.getProtocolVersion()).thenReturn(HttpVersion.HTTP_1_1);
		handler = new RestServerHandler(restMapper);
	}

	@Test
	public void testExceptionCaught() throws Exception {
		handler.exceptionCaught(ctx, new Exception());
		Mockito.verify(channel).close();
	}	

	@Test
	public void testSend100Continue() {
		handler.send100Continue(ctx);
		ArgumentCaptor<FullHttpResponse> captor = ArgumentCaptor.forClass(FullHttpResponse.class);
		Mockito.verify(ctx).write(captor.capture());
		FullHttpResponse response = captor.getValue();
		assertEquals(HttpResponseStatus.CONTINUE, response.getStatus());
	}	
	
	@Test
	public void testGetRestHandlerInvalidUri() {
		Mockito.when(request.getUri()).thenReturn(INVALID_URI);
		assertNull(handler.getRestHandler(request));
		Mockito.verifyZeroInteractions(restMapper);
	}

	@Test
	public void testGetRestHandlerUnknownUri() throws Exception {
		String UNKNOWN_URI = "http://example.com/unknown";
		URI uri = new URI(UNKNOWN_URI);
		Mockito.when(request.getUri()).thenReturn(UNKNOWN_URI);
		Mockito.when(restMapper.getHandler(Matchers.eq(uri))).thenReturn(null);
		assertNull(handler.getRestHandler(request));
	}

	@Test
	public void testGetRestHandlerGoodUri() throws Exception {
		String GOOD_URI = "http://example.com/good";
		URI uri = new URI(GOOD_URI);
		Mockito.when(request.getUri()).thenReturn(GOOD_URI);
		Mockito.when(restMapper.getHandler(Matchers.eq(uri))).thenReturn(restHandler);
		assertEquals(restHandler, handler.getRestHandler(request));
	}	

	@Test
	public void testProcessError() {
		HttpResponseStatus status = HttpResponseStatus.BAD_REQUEST;
		RestException e = new RestException(status);
		ArgumentCaptor<FullHttpResponse> captor = ArgumentCaptor.forClass(FullHttpResponse.class);
		Mockito.when(channel.writeAndFlush(Matchers.any(FullHttpResponse.class))).thenReturn(channelFuture);
		handler.processError(ctx, e);
		Mockito.verify(channel).writeAndFlush(captor.capture());
		Mockito.verify(channel).close();
		FullHttpResponse response = captor.getValue();
		assertEquals(status, response.getStatus());
		assertEquals(RestServerHandler.TEXT_PLAIN_UTF8, response.headers().get(HttpHeaders.Names.CONTENT_TYPE));
		Mockito.verify(channelFuture).addListener(Matchers.eq(ChannelFutureListener.CLOSE));
	}
	
	@Test
	public void testProcessHttpRequest() {
		// TODO
	}
	
}
