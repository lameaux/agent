package com.euromoby.cdn;

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
import com.euromoby.model.AgentId;
import com.euromoby.rest.RestException;
import com.euromoby.rest.handler.file.FileResponse;
import com.euromoby.rest.handler.fileinfo.FileInfo;
import com.euromoby.utils.StringUtils;

public class CdnServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

	private static final Logger LOG = LoggerFactory.getLogger(CdnServerHandler.class);	
	
	private FileProvider fileProvider;
	private MimeHelper mimeHelper;	
	private CdnNetwork cdnNetwork;

	public CdnServerHandler(FileProvider fileProvider, MimeHelper mimeHelper, CdnNetwork cdnNetwork) {
		this.fileProvider = fileProvider;
		this.mimeHelper = mimeHelper;
		this.cdnNetwork = cdnNetwork;
	}
	
	@Override
	public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
		if (!request.getMethod().equals(HttpMethod.GET)) {
			writeErrorResponse(ctx, HttpResponseStatus.NOT_IMPLEMENTED);
			return;
		}		

		URI uri = new URI(request.getUri());
		String fileLocation = uri.getPath();

		try {
			fileLocation = URLDecoder.decode(fileLocation, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			writeErrorResponse(ctx, HttpResponseStatus.BAD_REQUEST);
			return;
		}		

		
		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);		
		
		if (StringUtils.nullOrEmpty(fileLocation)) {
			writeErrorResponse(ctx, HttpResponseStatus.BAD_REQUEST);
			return;			
		}
		
		// remove first slash
		fileLocation = fileLocation.substring(1);
		
		File targetFile = fileProvider.getFileByLocation(fileLocation);
		if (targetFile == null) {
			
			// Ask other agents if they have file
			FileInfo fileInfo = cdnNetwork.find(uri.getPath());

			if (fileInfo != null) {
				AgentId agentId = fileInfo.getAgentId();
				String agentUrl = String.format("http://%s:%d%s", agentId.getHost(), agentId.getBasePort() + CdnServer.CDN_PORT, request.getUri()); 
				LOG.debug("Redirecting to {}", agentId);
				FullHttpResponse response = httpResponseProvider.createRedirectResponse(agentUrl);
				httpResponseProvider.writeResponse(ctx, response);				
				return;
			}
			
			// TODO create download job if file is not found
			// createJob();
			
			writeErrorResponse(ctx, HttpResponseStatus.NOT_FOUND);
			return;
		}
		
        // Cache Validation
		if (!HttpUtils.ifModifiedSince(request, targetFile)) {
			FullHttpResponse response = httpResponseProvider.createNotModifiedResponse();
			httpResponseProvider.writeResponse(ctx, response);
        	return;			
		}
		
		FileResponse fileResponse = new FileResponse(request, mimeHelper);
		try {
			fileResponse.send(ctx, targetFile);		
		} catch (RestException e) {
			writeErrorResponse(ctx, HttpResponseStatus.BAD_REQUEST, e.getMessage());
		}
	}

	private void writeErrorResponse(ChannelHandlerContext ctx, HttpResponseStatus status) {
		writeErrorResponse(ctx, status, status.reasonPhrase());
	}
	
	private void writeErrorResponse(ChannelHandlerContext ctx, HttpResponseStatus status, String message) {
		
		// Build the response object.
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, HttpUtils.fromString(message));
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
		// Write the response.
		ChannelFuture future = ctx.channel().writeAndFlush(response);
		future.addListener(ChannelFutureListener.CLOSE);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.channel().close();
	}

}