package com.euromoby.download.rest;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.Config;
import com.euromoby.download.DownloadManager;
import com.euromoby.http.HttpResponseProvider;
import com.euromoby.http.HttpUtils;
import com.euromoby.rest.RestException;
import com.euromoby.rest.handler.RestHandlerBase;
import com.euromoby.utils.IOUtils;
import com.euromoby.utils.ListUtils;
import com.euromoby.utils.StringUtils;

@Component
public class DownloadRequestHandler extends RestHandlerBase {

	public static final String URL = "/download/request";

	public static final String REQUEST_INPUT_URL = "url";
	public static final String REQUEST_INPUT_FILE_LOCATION = "file_location";

	private Config config;
	private DownloadManager downloadManager;
	
	@Autowired
	public DownloadRequestHandler(Config config, DownloadManager downloadManager) {
		this.config = config;
		this.downloadManager = downloadManager;
	}
	
	@Override
	public boolean matchUri(URI uri) {
		return uri.getPath().equals(URL);
	}
	
	@Override
	public FullHttpResponse doGet(ChannelHandlerContext ctx, HttpRequest request, Map<String, List<String>> queryParameters) {
		InputStream is = DownloadRequestHandler.class.getResourceAsStream("downloadrequest.html");
		String pageContent = IOUtils.streamToString(is);
		pageContent = pageContent.replace("%DOWNLOAD_PATH%", config.getAgentFilesPath());
		pageContent = pageContent.replace("%FILE_SEPARATOR%", File.separator);
		ByteBuf content = Unpooled.copiedBuffer(pageContent, CharsetUtil.UTF_8);
		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
		return httpResponseProvider.createHttpResponse(HttpResponseStatus.OK, content);
	}

	@Override
	public FullHttpResponse doPost(ChannelHandlerContext ctx, HttpRequest request, Map<String, List<String>> queryParameters, Map<String, List<String>> postParameters, Map<String, File> uploadFiles) throws RestException {
		validateRequestParameters(postParameters);
		String url = ListUtils.getFirst(postParameters.get(REQUEST_INPUT_URL));
		String fileLocation = ListUtils.getFirst(postParameters.get(REQUEST_INPUT_FILE_LOCATION));
		downloadManager.scheduleDownloadFile(url, fileLocation, false);

		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
		return httpResponseProvider.createHttpResponse(HttpResponseStatus.OK, HttpUtils.fromString("OK"));
	}

	protected void validateRequestParameters(Map<String, List<String>> requestParameters) throws RestException {
		if (StringUtils.nullOrEmpty(ListUtils.getFirst(requestParameters.get(REQUEST_INPUT_URL)))) {
			throw new RestException("Parameter is missing: " + REQUEST_INPUT_URL);
		}
		if (StringUtils.nullOrEmpty(ListUtils.getFirst(requestParameters.get(REQUEST_INPUT_FILE_LOCATION)))) {
			throw new RestException("Parameter is missing: " + REQUEST_INPUT_FILE_LOCATION);
		}
	}

}
