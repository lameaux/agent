package com.euromoby.rest.handler.fileinfo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.Config;
import com.euromoby.download.DownloadManager;
import com.euromoby.download.model.DownloadFile;
import com.euromoby.file.FileProvider;
import com.euromoby.http.HttpResponseProvider;
import com.euromoby.rest.RestException;
import com.euromoby.rest.handler.RestHandlerBase;
import com.google.gson.Gson;

@Component
public class FileInfoHandler extends RestHandlerBase {

	public static final String URL = "/filesinfo";
	private static final Pattern URL_PATTERN = Pattern.compile(URL + "/(.*)");
	private static final Gson gson = new Gson();
	
	private Config config;
	private DownloadManager downloadManager;
	private FileProvider fileProvider;

	@Autowired
	public FileInfoHandler(Config config, DownloadManager downloadManager, FileProvider fileProvider) {
		this.config = config;
		this.downloadManager = downloadManager;
		this.fileProvider = fileProvider;
	}

	@Override
	public boolean matchUri(URI uri) {
		Matcher m = URL_PATTERN.matcher(uri.getPath());
		return m.matches();
	}

	@Override
	public FullHttpResponse doGet(ChannelHandlerContext ctx, HttpRequest request, Map<String, List<String>> queryParameters) throws Exception {

		URI uri = new URI(request.getUri());

		Matcher m = URL_PATTERN.matcher(uri.getPath());
		if (!m.matches()) {
			throw new RestException(HttpResponseStatus.NOT_FOUND, "Not found");
		}
		String fileLocation = m.group(1);

		try {
			fileLocation = URLDecoder.decode(fileLocation, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RestException("Invalid request");
		}		
		
		FileInfo fileInfo;
		File targetFile = fileProvider.getFileByLocation(fileLocation);
		if (targetFile != null && targetFile.exists()) {
			fileInfo = existingFileInfo(targetFile, fileLocation);
		} else {
			DownloadFile downloadFile = downloadManager.findScheduledFileLocation(fileLocation);
			if (downloadFile == null) {
				throw new RestException(HttpResponseStatus.NOT_FOUND, "Not found");
			}
			fileInfo = scheduledFileInfo(fileLocation);
		} 
		
		String jsonResponse = gson.toJson(fileInfo);
		ByteBuf content = Unpooled.copiedBuffer(jsonResponse, CharsetUtil.UTF_8);
		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
		FullHttpResponse response = httpResponseProvider.createHttpResponse(HttpResponseStatus.OK, content);
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json; charset=UTF-8");
		return response;
	}
	
	protected FileInfo existingFileInfo(File file, String fileLocation) {
		FileInfo fileInfo = new FileInfo();
		fileInfo.setAgentId(config.getAgentId());
		fileInfo.setFileLocation(fileLocation);
		fileInfo.setLength(file.length());
		fileInfo.setLastModified(file.lastModified());
		fileInfo.setComplete(true);
		return fileInfo;
	}

	protected FileInfo scheduledFileInfo(String fileLocation) {
		FileInfo fileInfo = new FileInfo();
		fileInfo.setAgentId(config.getAgentId());
		fileInfo.setFileLocation(fileLocation);
		fileInfo.setComplete(false);
		return fileInfo;
	}	
	
}
