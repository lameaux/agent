package com.euromoby.rest.handler.upload;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.Config;
import com.euromoby.http.HttpResponseProvider;
import com.euromoby.http.HttpUtils;
import com.euromoby.rest.RestException;
import com.euromoby.rest.handler.RestHandlerBase;
import com.euromoby.utils.IOUtils;
import com.euromoby.utils.ListUtils;
import com.euromoby.utils.StringUtils;

@Component
public class UploadHandler extends RestHandlerBase {

	public static final String URL = "/upload";

	private static final String REQUEST_INPUT_LOCATION = "location";
	private static final String REQUEST_INPUT_FILE = "file";

	private String uploadPath;

	private static final Logger LOG = LoggerFactory.getLogger(UploadHandler.class);

	@Override
	public boolean matchUri(URI uri) {
		return uri.getPath().equals(URL);
	}	
	
	@Autowired
	public UploadHandler(Config config) {
		this.uploadPath = config.getAgentFilesPath();
	}

	@Override
	public FullHttpResponse doGet(ChannelHandlerContext ctx, HttpRequest request, Map<String, List<String>> queryParameters) {
		InputStream is = UploadHandler.class.getResourceAsStream("upload.html");
		String pageContent = IOUtils.streamToString(is);
		pageContent = pageContent.replace("%UPLOAD_PATH%", uploadPath);
		pageContent = pageContent.replace("%FILE_SEPARATOR%", File.separator);
		ByteBuf content = Unpooled.copiedBuffer(pageContent, CharsetUtil.UTF_8);
		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
		return httpResponseProvider.createHttpResponse(HttpResponseStatus.OK, content);
	}

	@Override
	public FullHttpResponse doPost(ChannelHandlerContext ctx, HttpRequest request, Map<String, List<String>> queryParameters, Map<String, List<String>> postParameters, Map<String, File> uploadFiles) throws RestException, IOException {

		String location = ListUtils.getFirst(postParameters.get(REQUEST_INPUT_LOCATION));
		File tempUploadedFile = uploadFiles.get(REQUEST_INPUT_FILE);

		if (StringUtils.nullOrEmpty(location)) {
			throw new RestException("Parameter is missing: " + REQUEST_INPUT_LOCATION);
		}

		if (tempUploadedFile == null) {
			throw new RestException("Parameter is missing: " + REQUEST_INPUT_FILE);
		}

		File targetFile = new File(new File(uploadPath), location);
		File parentDir = targetFile.getParentFile();
		if (!parentDir.exists() && !parentDir.mkdirs()) {
			throw new RestException("Unable to store file");
		}

		FileUtils.copyFile(tempUploadedFile, targetFile);
		LOG.info("Uploaded file " + targetFile.getPath());

		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
		return httpResponseProvider.createHttpResponse(HttpResponseStatus.OK, HttpUtils.fromString("OK"));
	}


}
