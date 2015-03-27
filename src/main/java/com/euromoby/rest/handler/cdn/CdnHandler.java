package com.euromoby.rest.handler.cdn;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.Config;
import com.euromoby.rest.RestException;
import com.euromoby.rest.handler.RestHandlerBase;

@Component
public class CdnHandler extends RestHandlerBase {

	public static final String URL = "/cdn";
	private static final Pattern URL_PATTERN = Pattern.compile("/cdn/(.+)");

	private Config config;

	@Autowired
	public CdnHandler(Config config) {
		this.config = config;
	}

	@Override
	public boolean matchUri(URI uri) {
		Matcher m = URL_PATTERN.matcher(uri.getPath());
		return m.matches();
	}

	@Override
	public FullHttpResponse doGet() throws RestException, URISyntaxException, IOException {

		URI uri = new URI(request.getUri());

		Matcher m = URL_PATTERN.matcher(uri.getPath());
		if (!m.matches()) {
			throw new RestException(HttpResponseStatus.NOT_FOUND, "Not found");
		}

		String fileName = m.group(1);
		
		File filesPath = new File(config.getAgentFilesPath());
		File targetFile = new File(filesPath, fileName);
		
		if (!targetFile.exists() || targetFile.isDirectory()) {
			throw new RestException(HttpResponseStatus.NOT_FOUND, "Not found");
		}

		
		
		ByteBuf fileContent = Unpooled.copiedBuffer(FileUtils.readFileToByteArray(targetFile));
		
		FullHttpResponse response = createHttpResponse(HttpResponseStatus.OK, fileContent);
		setContentTypeHeader(response, targetFile);
		HttpHeaders.setContentLength(response, targetFile.length());
		
		return response;
	}

	private static void setContentTypeHeader(HttpResponse response, File file) {
		MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE, mimeTypesMap.getContentType(file.getPath()));
	}

}
