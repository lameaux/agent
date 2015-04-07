package com.euromoby.http;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;

import com.euromoby.agent.Config;
import com.euromoby.model.Tuple;
import com.euromoby.utils.StringUtils;

public class AuthenticationHandler extends SimpleChannelInboundHandler<HttpObject> {

	public static final String AUTH_BASIC = "Basic";
	
	private Config config;
	public AuthenticationHandler(Config config) {
		this.config = config;
	}
	
	@Override
	public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
		if (config.isRestSecured()) {
			if (msg instanceof HttpRequest) {
				HttpRequest request = (HttpRequest) msg;
				if (!isAuthenticated(request)) {
					HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
					FullHttpResponse response = httpResponseProvider.createUnauthorizedResponse(config.getRestRealm());
					ChannelFuture future = ctx.channel().writeAndFlush(response);
					future.addListener(ChannelFutureListener.CLOSE);					
					return;
				}
			}
		}
		ctx.fireChannelRead(msg);
	}
	
	private boolean isAuthenticated(HttpRequest request) {
		String authorization = request.headers().get(HttpHeaders.Names.AUTHORIZATION);
		if (StringUtils.nullOrEmpty(authorization)) {
			return false;
		}

		Tuple<String, String> authHeader = Tuple.splitString(authorization, " ");
		if (AUTH_BASIC.equals(authHeader.getFirst())) {
			String authDecoded = new String(Base64.decodeBase64(authHeader.getSecond()), Charsets.UTF_8);
			Tuple<String, String> nameAndPassword = Tuple.splitString(authDecoded, ":");
			return config.getRestLogin().equals(nameAndPassword.getFirst()) && config.getRestPassword().equals(nameAndPassword.getSecond());
		}
		
		return false;
	}

	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.channel().close();
	}
}
