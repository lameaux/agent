package com.euromoby.proxy;

import static org.junit.Assert.assertEquals;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.euromoby.http.AsyncHttpClientProvider;

@RunWith(MockitoJUnitRunner.class)
public class ProxyResponseTest {

	@Mock
	AsyncHttpClientProvider asyncHttpClientProvider; 
	@Mock
	ChannelHandlerContext ctx;
	@Mock
	Channel channel;
	@Mock
	FullHttpRequest request;
	@Mock
	ChannelFuture channelFuture;	

	ProxyResponse proxyResponse;

	
	@Before
	public void init() {
		Mockito.when(ctx.channel()).thenReturn(channel);
		proxyResponse = new ProxyResponse(asyncHttpClientProvider);
	}
	
	@Test
	public void testProxy() {
		Mockito.when(channel.writeAndFlush(Matchers.any(FullHttpResponse.class))).thenReturn(channelFuture);
		proxyResponse.proxy(ctx, request, "http://example.com");

		ArgumentCaptor<DefaultFullHttpResponse> responseCaptor = ArgumentCaptor.forClass(DefaultFullHttpResponse.class);
		Mockito.verify(channel).writeAndFlush(responseCaptor.capture());
		FullHttpResponse response = responseCaptor.getValue();
		assertEquals(HttpResponseStatus.GATEWAY_TIMEOUT, response.getStatus());
	}
	
}
