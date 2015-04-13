package com.euromoby.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.ssl.SslHandler;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.euromoby.file.MimeHelper;

@RunWith(MockitoJUnitRunner.class)
public class FileResponseTest {

	FileResponse fileResponse;
	@Mock
	HttpRequest httpRequest;
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
		
		Mockito.when(httpResponse.headers()).thenReturn(responseHeaders);
		fileResponse.setupResponseHeaders(httpResponse);
		
		Mockito.verify(responseHeaders).set(Matchers.eq(NAME), Matchers.eq(VALUE));
	}
	
}
