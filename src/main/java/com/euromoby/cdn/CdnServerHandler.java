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
import java.net.URI;
import java.net.URLDecoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.euromoby.file.FileProvider;
import com.euromoby.file.MimeHelper;
import com.euromoby.http.FileResponse;
import com.euromoby.http.HttpResponseProvider;
import com.euromoby.http.HttpUtils;
import com.euromoby.model.AgentId;
import com.euromoby.model.Tuple;
import com.euromoby.rest.RestException;
import com.euromoby.rest.handler.fileinfo.FileInfo;
import com.euromoby.utils.StringUtils;

public class CdnServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

	private static final Logger LOG = LoggerFactory.getLogger(CdnServerHandler.class);	
	
	private FileProvider fileProvider;
	private MimeHelper mimeHelper;	
	private CdnNetwork cdnNetwork;
	private HttpResponseProvider httpResponseProvider = new HttpResponseProvider();

	public CdnServerHandler(FileProvider fileProvider, MimeHelper mimeHelper, CdnNetwork cdnNetwork) {
		this.fileProvider = fileProvider;
		this.mimeHelper = mimeHelper;
		this.cdnNetwork = cdnNetwork;
	}
	
	protected void manageCdnRequest(ChannelHandlerContext ctx, URI uri) {
		
		// Ask other agents if they have file
		LOG.debug("Asking other agents for {}", uri.getPath());
		Tuple<CdnResource, FileInfo> searchResult = cdnNetwork.find(uri.getPath());
		
		CdnResource cdnResource = searchResult.getFirst();
		if (cdnResource == null) {
			writeErrorResponse(ctx, HttpResponseStatus.NOT_FOUND);
			return;
		}
		
		FileInfo fileInfo = searchResult.getSecond();
		if (fileInfo != null) {
			AgentId agentId = fileInfo.getAgentId();
			String agentUrl = String.format("http://%s:%d%s", agentId.getHost(), agentId.getBasePort() + CdnServer.CDN_PORT, uri.getPath()); 
			LOG.debug("Redirecting to {}", agentId);
			FullHttpResponse response = httpResponseProvider.createRedirectResponse(agentUrl);
			httpResponseProvider.writeResponse(ctx, response);				
			return;
		}

		// TODO Stream (+ store local) from source location if defined, 
		// no range if not synced, 
		// or 404 if not found in source
					
		
		// create download job or start streaming
		String sourceUrl = cdnResource.getSourceUrl(uri.getPath());
		if (sourceUrl != null) {
			LOG.debug("Redirecting to {}", sourceUrl);
			FullHttpResponse response = httpResponseProvider.createRedirectResponse(sourceUrl);
			httpResponseProvider.writeResponse(ctx, response);				
			return;				
		}
		
		// nothing found
		writeErrorResponse(ctx, HttpResponseStatus.NOT_FOUND);		
	}
	
	protected void manageFileResponse(ChannelHandlerContext ctx, FullHttpRequest request, File targetFile) {
		FileResponse fileResponse = new FileResponse(request, mimeHelper);
		try {
			fileResponse.send(ctx, targetFile);		
		} catch (RestException e) {
			writeErrorResponse(ctx, HttpResponseStatus.BAD_REQUEST, e.getMessage());
		}		
	}
	
	@Override
	public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {

		if (HttpHeaders.is100ContinueExpected(request)) {
			send100Continue(ctx);
		}		
		
		httpResponseProvider.setHttpRequest(request);		
		
		if (!request.getMethod().equals(HttpMethod.GET)) {
			writeErrorResponse(ctx, HttpResponseStatus.NOT_IMPLEMENTED);
			return;
		}		

		URI uri;
		String fileLocation;

		try {
			uri = new URI(request.getUri());
			fileLocation = URLDecoder.decode(uri.getPath(), "UTF-8");
		} catch (Exception e) {
			writeErrorResponse(ctx, HttpResponseStatus.BAD_REQUEST);
			return;
		}		

		if (StringUtils.nullOrEmpty(fileLocation) || !fileLocation.startsWith("/")) {
			writeErrorResponse(ctx, HttpResponseStatus.BAD_REQUEST);
			return;			
		}
		// remove first slash
		fileLocation = fileLocation.substring(1);

		File targetFile = fileProvider.getFileByLocation(fileLocation);
		if (targetFile == null) {
			manageCdnRequest(ctx, uri);
			return;
		}
		
        // Cache Validation
		if (!HttpUtils.ifModifiedSince(request, targetFile)) {
			FullHttpResponse response = httpResponseProvider.createNotModifiedResponse();
			httpResponseProvider.writeResponse(ctx, response);
        	return;			
		}
		
		manageFileResponse(ctx, request, targetFile);
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

	protected void send100Continue(ChannelHandlerContext ctx) {
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
		ctx.write(response);
	}	
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.channel().close();
	}

}