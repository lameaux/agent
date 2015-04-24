package com.euromoby.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.euromoby.agent.Config;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationHandlerTest {

	@Mock
	Config config;
	@Mock 
	ChannelHandlerContext ctx;
	@Mock
	Channel channel;
	@Mock
	HttpRequest request;
	@Mock
	HttpHeaders headers;
	@Mock
	ChannelFuture future;
	
	AuthenticationHandler authenticationHandler;
	
	@Before
	public void init() {
		Mockito.when(ctx.channel()).thenReturn(channel);
		Mockito.when(request.headers()).thenReturn(headers);
		Mockito.when(request.getProtocolVersion()).thenReturn(HttpVersion.HTTP_1_1);
		authenticationHandler = new AuthenticationHandler(config);
	}
	
	@Test
	public void testExceptionCaught() throws Exception {
		authenticationHandler.exceptionCaught(ctx, null);
		Mockito.verify(channel).close();
	}

	@Test
	public void noAuthHeader() {
		Mockito.when(headers.get(Matchers.eq(HttpHeaders.Names.AUTHORIZATION))).thenReturn(null);
		assertFalse(authenticationHandler.isAuthenticated(request));
	}

	@Test
	public void notBasicAuthHeader() {
		String authHeader = "Digest foobar";
		Mockito.when(headers.get(Matchers.eq(HttpHeaders.Names.AUTHORIZATION))).thenReturn(authHeader);
		assertFalse(authenticationHandler.isAuthenticated(request));
	}	

	@Test
	public void notBasicInvalidBothCredentials() throws Exception {
		String user = "user";
		String password = "password";
		String authHeader = AuthenticationHandler.AUTH_BASIC + " " + Base64.encodeBase64String( (user+":"+password).getBytes(Charsets.UTF_8));
		Mockito.when(headers.get(Matchers.eq(HttpHeaders.Names.AUTHORIZATION))).thenReturn(authHeader);
		Mockito.when(config.getRestLogin()).thenReturn("");
		Mockito.when(config.getRestPassword()).thenReturn("");		
		assertFalse(authenticationHandler.isAuthenticated(request));
	}	

	@Test
	public void notBasicInvalidUser() throws Exception {
		String user = "user";
		String password = "password";
		String authHeader = AuthenticationHandler.AUTH_BASIC + " " + Base64.encodeBase64String( (user+":"+password).getBytes(Charsets.UTF_8));
		Mockito.when(headers.get(Matchers.eq(HttpHeaders.Names.AUTHORIZATION))).thenReturn(authHeader);
		Mockito.when(config.getRestLogin()).thenReturn("");
		Mockito.when(config.getRestPassword()).thenReturn(password);		
		assertFalse(authenticationHandler.isAuthenticated(request));
	}	

	@Test
	public void notBasicInvalidPassword() throws Exception {
		String user = "user";
		String password = "password";
		String authHeader = AuthenticationHandler.AUTH_BASIC + " " + Base64.encodeBase64String( (user+":"+password).getBytes(Charsets.UTF_8));
		Mockito.when(headers.get(Matchers.eq(HttpHeaders.Names.AUTHORIZATION))).thenReturn(authHeader);
		Mockito.when(config.getRestLogin()).thenReturn(user);
		Mockito.when(config.getRestPassword()).thenReturn("");		
		assertFalse(authenticationHandler.isAuthenticated(request));
	}	
	
	
	@Test
	public void notBasicGoodCredentials() throws Exception {
		String user = "user";
		String password = "password";
		String authHeader = AuthenticationHandler.AUTH_BASIC + " " + Base64.encodeBase64String( (user+":"+password).getBytes(Charsets.UTF_8));
		Mockito.when(headers.get(Matchers.eq(HttpHeaders.Names.AUTHORIZATION))).thenReturn(authHeader);
		Mockito.when(config.getRestLogin()).thenReturn(user);
		Mockito.when(config.getRestPassword()).thenReturn(password);		
		assertTrue(authenticationHandler.isAuthenticated(request));
	}	
	
	@Test
	public void restIsNotSecured() throws Exception {
		Mockito.when(config.isRestSecured()).thenReturn(false);
		authenticationHandler.channelRead0(ctx, request);
		Mockito.verify(ctx).fireChannelRead(Matchers.eq(request));
	}

	@Test
	public void restIsSecuredAuthFailed() throws Exception {
		String REALM = "realm";
		Mockito.when(config.isRestSecured()).thenReturn(true);
		Mockito.when(config.getRestRealm()).thenReturn(REALM);
		Mockito.when(headers.get(Matchers.eq(HttpHeaders.Names.AUTHORIZATION))).thenReturn(null);	
		Mockito.when(channel.writeAndFlush(Matchers.any(FullHttpResponse.class))).thenReturn(future);		
		authenticationHandler.channelRead0(ctx, request);
		Mockito.verify(ctx, Mockito.never()).fireChannelRead(Matchers.eq(request));
		
		ArgumentCaptor<FullHttpResponse> captor = ArgumentCaptor.forClass(FullHttpResponse.class);
		Mockito.verify(channel).writeAndFlush(captor.capture());
		
		FullHttpResponse response = captor.getValue();
		assertEquals(HttpResponseStatus.UNAUTHORIZED, response.getStatus());
	}	

	@Test
	public void restIsSecuredAuthSuccess() throws Exception {
		Mockito.when(config.isRestSecured()).thenReturn(true);
		String user = "user";
		String password = "password";
		String authHeader = AuthenticationHandler.AUTH_BASIC + " " + Base64.encodeBase64String( (user+":"+password).getBytes(Charsets.UTF_8));
		Mockito.when(headers.get(Matchers.eq(HttpHeaders.Names.AUTHORIZATION))).thenReturn(authHeader);
		Mockito.when(config.getRestLogin()).thenReturn(user);
		Mockito.when(config.getRestPassword()).thenReturn(password);		
		authenticationHandler.channelRead0(ctx, request);
		Mockito.verify(ctx).fireChannelRead(Matchers.eq(request));
	}	
	
}
