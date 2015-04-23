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
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.euromoby.download.DownloadJob;
import com.euromoby.file.FileProvider;
import com.euromoby.file.MimeHelper;
import com.euromoby.http.FileResponse;
import com.euromoby.http.HttpResponseProvider;
import com.euromoby.http.HttpUtils;
import com.euromoby.job.JobDetail;
import com.euromoby.job.JobManager;
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
	private JobManager jobManager;

	public CdnServerHandler(FileProvider fileProvider, MimeHelper mimeHelper, CdnNetwork cdnNetwork, JobManager jobManager) {
		this.fileProvider = fileProvider;
		this.mimeHelper = mimeHelper;
		this.cdnNetwork = cdnNetwork;
		this.jobManager = jobManager;
	}
	
	protected void manageCdnRequest(ChannelHandlerContext ctx, FullHttpRequest httpRequest, URI uri, String fileLocation) {
		
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
			manageRedirect(ctx, httpRequest, buildAgentUrlLocation(fileInfo.getAgentId(), getPathWithQuery(uri)));
			return;
		}

		// has origin?
		String sourceUrl = cdnResource.getSourceUrl(getPathWithQuery(uri));
		if (sourceUrl != null) {

			if (cdnResource.isDownloadIfMissing()) {
				scheduleDownloadJob(sourceUrl, fileLocation);
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
	
	protected void scheduleDownloadJob(String sourceUrl, String fileLocation) {
		Map<String, String> parameters = new HashMap<String, String>();	
		parameters.put(DownloadJob.PARAM_URL, sourceUrl);
		parameters.put(DownloadJob.PARAM_LOCATION, fileLocation);
		JobDetail jobDetail = new JobDetail(DownloadJob.class, parameters);
		// TODO choose best agent
		//jobDetail.setRecipient(recipient);
		jobManager.submit(jobDetail);		
	}
	
	protected void manageContentProxying(ChannelHandlerContext ctx, FullHttpRequest httpRequest, String sourceUrl) {
		// TODO real proxying (AsyncClient)
		writeErrorResponse(ctx, HttpResponseStatus.GATEWAY_TIMEOUT);
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