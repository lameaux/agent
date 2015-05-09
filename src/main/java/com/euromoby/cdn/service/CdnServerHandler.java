package com.euromoby.cdn.service;

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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.euromoby.agent.AgentManager;
import com.euromoby.agent.Config;
import com.euromoby.agent.model.AgentStatus;
import com.euromoby.cdn.CdnNetwork;
import com.euromoby.cdn.model.CdnResource;
import com.euromoby.download.DownloadManager;
import com.euromoby.file.FileProvider;
import com.euromoby.file.MimeHelper;
import com.euromoby.http.FileResponse;
import com.euromoby.http.HttpResponseProvider;
import com.euromoby.http.HttpUtils;
import com.euromoby.model.AgentId;
import com.euromoby.model.Tuple;
import com.euromoby.proxy.ProxyResponseProvider;
import com.euromoby.rest.RestException;
import com.euromoby.rest.handler.fileinfo.FileInfo;
import com.euromoby.utils.StringUtils;
import com.euromoby.utils.SystemUtils;

public class CdnServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

	private static final Logger LOG = LoggerFactory.getLogger(CdnServerHandler.class);	
	
	private Config config;
	private FileProvider fileProvider;
	private MimeHelper mimeHelper;	
	private CdnNetwork cdnNetwork;
	private DownloadManager downloadManager;
	private AgentManager agentManager;
	private ProxyResponseProvider proxyResponseProvider;

	public CdnServerHandler(Config config, FileProvider fileProvider, MimeHelper mimeHelper, CdnNetwork cdnNetwork, DownloadManager downloadManager, AgentManager agentManager, ProxyResponseProvider proxyResponseProvider) {
		this.config = config;
		this.fileProvider = fileProvider;
		this.mimeHelper = mimeHelper;
		this.cdnNetwork = cdnNetwork;
		this.downloadManager = downloadManager;
		this.agentManager = agentManager;
		this.proxyResponseProvider = proxyResponseProvider;
	}
	
	protected void manageCdnRequest(ChannelHandlerContext ctx, FullHttpRequest httpRequest, URI uri, String fileLocation) {
		
		LOG.debug("Asking other agents for {}", uri.getPath());
		Tuple<CdnResource, FileInfo> searchResult = cdnNetwork.find(uri.getPath());
		
		CdnResource cdnResource = searchResult.getFirst();
		if (cdnResource == null) {
			writeErrorResponse(ctx, HttpResponseStatus.NOT_FOUND);
			return;
		}
		
		FileInfo fileInfo = searchResult.getSecond();
		if (fileInfo != null) {
			manageRedirect(ctx, httpRequest, buildAgentUrlLocation(fileInfo.getAgentId(), getPathWithQuery(uri)));
			return;
		}

		// has origin?
		String sourceUrl = cdnResource.getSourceUrl(getPathWithQuery(uri));
		if (sourceUrl != null) {
			if (cdnResource.isDownloadIfMissing()) {
				addToDownloadScheduler(sourceUrl, fileLocation);
			}
			if (cdnResource.isProxyable()) {
				manageContentProxying(ctx, httpRequest, sourceUrl);
			} else {
				manageRedirect(ctx, httpRequest, sourceUrl);
			}
			return;				
		}
		
		// nothing found
		writeErrorResponse(ctx, HttpResponseStatus.NOT_FOUND);		
	}
	
	protected String getPathWithQuery(URI uri) {
		String pathWithQuery = uri.getPath();
		String queryPart = uri.getQuery();
		if (!StringUtils.nullOrEmpty(queryPart)) {
			pathWithQuery = pathWithQuery + "?" + queryPart;
		}
		return pathWithQuery;
	}
	
	protected String buildAgentUrlLocation(AgentId agentId, String urlPathWithQuery) {
		return String.format("http://%s:%d%s", agentId.getHost(), agentId.getBasePort() + CdnServer.CDN_PORT, urlPathWithQuery);
	}
	
	protected void addToDownloadScheduler(String sourceUrl, String fileLocation) {
		AgentId agentId = findAgentWithFreeSpace();
		if (agentId == null) {
			downloadManager.scheduleDownloadFile(sourceUrl, fileLocation, false);
		} else {
			downloadManager.askAgentToDownloadFile(agentId, sourceUrl, fileLocation);
		}
	}
	
	protected AgentId findAgentWithFreeSpace() {
		AgentId agentId = null;
		long freeSpace = SystemUtils.getFreeSpace(config.getAgentFilesPath());
		List<AgentId> activeAgents = agentManager.getActive();
		for (AgentId activeAgentId : activeAgents) {
			AgentStatus agentStatus = agentManager.getAgentStatus(activeAgentId);
			if (agentStatus.getFreeSpace() > freeSpace) {
				agentId = activeAgentId;
				freeSpace = agentStatus.getFreeSpace();
			}
		}
		return agentId;
	}
	
	protected void manageContentProxying(ChannelHandlerContext ctx, FullHttpRequest httpRequest, String sourceUrl) {
		proxyResponseProvider.proxy(ctx, httpRequest, sourceUrl);	
	}
	
	protected void manageRedirect(ChannelHandlerContext ctx, FullHttpRequest httpRequest, String sourceUrl) {
		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(httpRequest);		
		FullHttpResponse response = httpResponseProvider.createRedirectResponse(sourceUrl);
		httpResponseProvider.writeResponse(ctx, response);		
	}
	
	protected void manageFileResponse(ChannelHandlerContext ctx, FullHttpRequest request, File targetFile) {
		FileResponse fileResponse = new FileResponse(request, mimeHelper);
		try {
			fileResponse.send(ctx, targetFile);		
		} catch (RestException e) {
			writeErrorResponse(ctx, e.getStatus(), e.getMessage());
		}		
	}
	
	@Override
	public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {

		if (HttpHeaders.is100ContinueExpected(request)) {
			send100Continue(ctx);
		}		
		
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
			manageCdnRequest(ctx, request, uri, fileLocation);
			return;
		}
		
        // Cache Validation
		if (!HttpUtils.ifModifiedSince(request, targetFile)) {
			HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
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