package com.euromoby.rest;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.DiskAttribute;
import io.netty.handler.codec.http.multipart.DiskFileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import io.netty.util.CharsetUtil;

import java.net.URI;
import java.net.URISyntaxException;

import com.euromoby.rest.handler.RestHandler;

public class RestServerHandler extends SimpleChannelInboundHandler<HttpObject> {

	public static final String TEXT_PLAIN_UTF8 = "text/plain; charset=UTF-8";
	
	private RestMapper restMapper;
	
	private RestHandler handler;
	private HttpPostRequestDecoder decoder;
	private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);

	static {
		DiskFileUpload.deleteOnExitTemporaryFile = true;
		DiskFileUpload.baseDirectory = null; // system temp directory
		DiskAttribute.deleteOnExitTemporaryFile = true;
		DiskAttribute.baseDirectory = null; // system temp directory
	}

	public RestServerHandler(RestMapper restMapper) {
		this.restMapper = restMapper;
	}

	protected HttpPostRequestDecoder getHttpPostRequestDecoder() {
		return decoder;
	}
	
	protected RestHandler getRestHandler() {
		return handler;
	}
	
	
	protected RestHandler findRestHandler(HttpRequest httpRequest) {
		String uriString = httpRequest.getUri();
		URI uri;
		try {
			uri = new URI(uriString);
		} catch (URISyntaxException e) {
			return null;
		}

		return restMapper.getHandler(uri);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if (decoder != null) {
			decoder.cleanFiles();
		}
	}

	protected void executeRestHandler(ChannelHandlerContext ctx) throws IllegalStateException {
		if (handler == null) {
			throw new IllegalStateException("RestHandler not configured");
		}
		handler.process(ctx);
	}

	protected void sendErrorResponse(ChannelHandlerContext ctx, RestException e) {
		ByteBuf outputBuf = Unpooled.copiedBuffer(e.getMessage(), CharsetUtil.UTF_8);
		
		// Build the response object.
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, e.getStatus(), outputBuf);
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE, TEXT_PLAIN_UTF8);

		// Write the response.
		ChannelFuture future = ctx.channel().writeAndFlush(response);
		future.addListener(ChannelFutureListener.CLOSE);		
		ctx.channel().close();
	}

	protected void processHttpRequest(ChannelHandlerContext ctx, HttpRequest request) {
		handler = findRestHandler(request);
		if (handler == null) {
			sendErrorResponse(ctx, new RestException(HttpResponseStatus.NOT_FOUND));
			return;
		}

		if (HttpHeaders.is100ContinueExpected(request)) {
			send100Continue(ctx);
		}			
		
		// save request
		handler.setHttpRequest(request);

		// if GET Method: should not try to create a HttpPostRequestDecoder
		if (request.getMethod().equals(HttpMethod.GET)) {
			return;
		}
		try {
			decoder = new HttpPostRequestDecoder(factory, request);
			// save decoder
			handler.setHttpPostRequestDecoder(decoder);
		} catch (ErrorDataDecoderException e) {
			sendErrorResponse(ctx, new RestException(e));
		}	
		
	}
	
	@Override
	public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
		if (msg instanceof HttpRequest) {
			HttpRequest request = (HttpRequest) msg;
			processHttpRequest(ctx, request);
			return;
		}

		// decoder is for POST
		if (decoder != null) {
			if (msg instanceof HttpContent) {
				// New chunk is received
				HttpContent chunk = (HttpContent) msg;
				try {
					decoder.offer(chunk);
				} catch (ErrorDataDecoderException e) {
					sendErrorResponse(ctx, new RestException(e));
					return;
				}

				// last chunk arrived
				if (chunk instanceof LastHttpContent) {
					executeRestHandler(ctx);
					reset();
				}
			}
		} else {
			// doing GET
			executeRestHandler(ctx);
		}
	}

	private void reset() {
		handler = null;
		decoder.destroy();
		decoder = null;
	}

	protected void send100Continue(ChannelHandlerContext ctx) {
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
		ctx.write(response);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.channel().close();
	}

}