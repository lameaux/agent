package com.euromoby.cdn;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.euromoby.file.FileProvider;
import com.euromoby.file.MimeHelper;
import com.euromoby.http.HttpResponseProvider;
import com.euromoby.http.HttpUtils;
import com.euromoby.rest.RestException;
import com.euromoby.rest.handler.file.FileResponse;

public class CdnServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

	private static final Logger LOG = LoggerFactory.getLogger(CdnServerHandler.class);
	
	private FileProvider fileProvider;
	private MimeHelper mimeHelper;	

	public CdnServerHandler(FileProvider fileProvider, MimeHelper mimeHelper) {
		this.fileProvider = fileProvider;
		this.mimeHelper = mimeHelper;
	}
	
	@Override
	public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
		if (!request.getMethod().equals(HttpMethod.GET)) {
			writeErrorResponse(ctx, HttpResponseStatus.NOT_IMPLEMENTED, Unpooled.EMPTY_BUFFER);
			return;
		}		

		URI uri = new URI(request.getUri());
		String fileLocation = uri.getPath();

		try {
			fileLocation = URLDecoder.decode(fileLocation, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			writeErrorResponse(ctx, HttpResponseStatus.BAD_REQUEST, HttpUtils.fromString("Invalid request"));
			return;
		}		
		
		File targetFile = fileProvider.getFileByLocation(fileLocation);
		if (targetFile == null) {
			
			// TODO ask other agents if they have file
			// TODO create download job if file is not found			
			
			writeErrorResponse(ctx, HttpResponseStatus.NOT_FOUND, HttpUtils.fromString("Not found"));
			return;
		}
		
        // Cache Validation
		if (!HttpUtils.ifModifiedSince(request, targetFile)) {
			HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);			
			FullHttpResponse response = httpResponseProvider.createNotModifiedResponse();
			httpResponseProvider.writeResponse(ctx, response);
        	return;			
		}
		
		FileResponse fileResponse = new FileResponse(request, mimeHelper);
		try {
			fileResponse.send(ctx, targetFile);		
		} catch (RestException e) {
			writeErrorResponse(ctx, HttpResponseStatus.BAD_REQUEST, HttpUtils.fromString(e.getMessage()));
		}
	}
	
	private void writeErrorResponse(ChannelHandlerContext ctx, HttpResponseStatus status, ByteBuf buf) {
		// Build the response object.
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, buf);
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
		// Write the response.
		ChannelFuture future = ctx.channel().writeAndFlush(response);
		future.addListener(ChannelFutureListener.CLOSE);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		//LOG.debug("Exception", cause);
		ctx.channel().close();
	}

}