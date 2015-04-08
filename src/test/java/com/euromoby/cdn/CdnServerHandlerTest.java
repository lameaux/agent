package com.euromoby.cdn;

import static org.junit.Assert.assertEquals;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

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

@RunWith(MockitoJUnitRunner.class)
public class CdnServerHandlerTest {

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
	ChannelFuture channelFuture;
	
	CdnServerHandler handler;

	@Before
	public void init() {
		Mockito.when(ctx.channel()).thenReturn(channel);
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
	

}
