package com.euromoby.cdn;

import static org.junit.Assert.assertEquals;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.euromoby.file.FileProvider;
import com.euromoby.file.MimeHelper;
import com.euromoby.http.HttpUtils;

@RunWith(MockitoJUnitRunner.class)
public class CdnServerHandlerTest {

	private static final String INVALID_URI = "$[level]/r$[y]_c$[x].jpg";

	@Mock
	FileProvider fileProvider;
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

	CdnServerHandler handler;

	@Before
	public void init() {
		Mockito.when(ctx.channel()).thenReturn(channel);
		Mockito.when(request.headers()).thenReturn(headers);
		Mockito.when(request.getProtocolVersion()).thenReturn(HttpVersion.HTTP_1_1);
		handler = new CdnServerHandler(fileProvider, mimeHelper, cdnNetwork);
	}

	@Test
	public void testExceptionCaught() throws Exception {
		handler.exceptionCaught(ctx, new Exception());
		Mockito.verify(channel).close();
	}

	@Test
	public void testInvalidHttpMethod() throws Exception {
		Mockito.when(request.getMethod()).thenReturn(HttpMethod.POST);
		Mockito.when(channel.writeAndFlush(Matchers.any(DefaultFullHttpResponse.class))).thenReturn(channelFuture);
		ArgumentCaptor<DefaultFullHttpResponse> responseCaptor = ArgumentCaptor.forClass(DefaultFullHttpResponse.class);
		handler.channelRead0(ctx, request);
		Mockito.verify(channel).writeAndFlush(responseCaptor.capture());
		FullHttpResponse response = responseCaptor.getValue();
		assertEquals(HttpResponseStatus.NOT_IMPLEMENTED, response.getStatus());
	}

	@Test
	public void testInvalidUri() throws Exception {
		Mockito.when(request.getMethod()).thenReturn(HttpMethod.GET);
		Mockito.when(request.getUri()).thenReturn(INVALID_URI);
		Mockito.when(channel.writeAndFlush(Matchers.any(DefaultFullHttpResponse.class))).thenReturn(channelFuture);
		ArgumentCaptor<DefaultFullHttpResponse> responseCaptor = ArgumentCaptor.forClass(DefaultFullHttpResponse.class);
		handler.channelRead0(ctx, request);
		Mockito.verify(channel).writeAndFlush(responseCaptor.capture());
		FullHttpResponse response = responseCaptor.getValue();
		assertEquals(HttpResponseStatus.BAD_REQUEST, response.getStatus());
	}

	@Test
	public void testEmptyFileLocation() throws Exception {
		Mockito.when(request.getMethod()).thenReturn(HttpMethod.GET);
		Mockito.when(request.getUri()).thenReturn("");
		Mockito.when(channel.writeAndFlush(Matchers.any(DefaultFullHttpResponse.class))).thenReturn(channelFuture);
		ArgumentCaptor<DefaultFullHttpResponse> responseCaptor = ArgumentCaptor.forClass(DefaultFullHttpResponse.class);
		handler.channelRead0(ctx, request);
		Mockito.verify(channel).writeAndFlush(responseCaptor.capture());
		FullHttpResponse response = responseCaptor.getValue();
		assertEquals(HttpResponseStatus.BAD_REQUEST, response.getStatus());
	}

	@Test
	public void testWrongFileLocation() throws Exception {
		Mockito.when(request.getMethod()).thenReturn(HttpMethod.GET);
		Mockito.when(request.getUri()).thenReturn("foo");
		Mockito.when(channel.writeAndFlush(Matchers.any(DefaultFullHttpResponse.class))).thenReturn(channelFuture);
		ArgumentCaptor<DefaultFullHttpResponse> responseCaptor = ArgumentCaptor.forClass(DefaultFullHttpResponse.class);
		handler.channelRead0(ctx, request);
		Mockito.verify(channel).writeAndFlush(responseCaptor.capture());
		FullHttpResponse response = responseCaptor.getValue();
		assertEquals(HttpResponseStatus.BAD_REQUEST, response.getStatus());
	}

	@Test
	public void testCacheNotModified() throws Exception {
		String FILE = "file";
		Mockito.when(request.getMethod()).thenReturn(HttpMethod.GET);
		Mockito.when(request.getUri()).thenReturn("/" + FILE);
		Mockito.when(channel.writeAndFlush(Matchers.any(DefaultFullHttpResponse.class))).thenReturn(channelFuture);
		long lastModified = System.currentTimeMillis();
		Mockito.when(targetFile.lastModified()).thenReturn(lastModified);
		Mockito.when(headers.get(Matchers.eq(HttpHeaders.Names.IF_MODIFIED_SINCE))).thenReturn(
				new SimpleDateFormat(HttpUtils.HTTP_DATE_FORMAT, Locale.US).format(new Date(lastModified)));
		ArgumentCaptor<DefaultFullHttpResponse> responseCaptor = ArgumentCaptor.forClass(DefaultFullHttpResponse.class);
		Mockito.when(fileProvider.getFileByLocation(Matchers.eq(FILE))).thenReturn(targetFile);

		handler.channelRead0(ctx, request);
		Mockito.verify(channel).writeAndFlush(responseCaptor.capture());
		FullHttpResponse response = responseCaptor.getValue();
		assertEquals(HttpResponseStatus.NOT_MODIFIED, response.getStatus());
	}

}
