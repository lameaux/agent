package com.euromoby.rest.handler.fileinfo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.file.FileProvider;
import com.euromoby.rest.RestException;
import com.euromoby.rest.handler.RestHandlerBase;
import com.google.gson.Gson;

@Component
public class FileInfoHandler extends RestHandlerBase {

	public static final String URL = "/fileinfo";
	private static final Pattern URL_PATTERN = Pattern.compile(URL + "/(.+)");
	private static final Gson gson = new Gson();
	
	private FileProvider fileProvider;

	@Autowired
	public FileInfoHandler(FileProvider fileProvider) {
		this.fileProvider = fileProvider;
	}

	@Override
	public boolean matchUri(URI uri) {
		Matcher m = URL_PATTERN.matcher(uri.getPath());
		return m.matches();
	}

	@Override
	public FullHttpResponse doGet() throws Exception {

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
		
		File targetFile = fileProvider.getFileByLocation(fileLocation);
		if (targetFile == null) {
			throw new RestException(HttpResponseStatus.NOT_FOUND, "Not found");
		}
		
		FileInfoResponse fileInfoResponse = new FileInfoResponse();
		fileInfoResponse.setLength(targetFile.length());
		fileInfoResponse.setLastModified(targetFile.lastModified());
		
		String jsonResponse = gson.toJson(fileInfoResponse);
		ByteBuf content = Unpooled.copiedBuffer(jsonResponse, CharsetUtil.UTF_8);
		FullHttpResponse response = createHttpResponse(HttpResponseStatus.OK, content);
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json; charset=UTF-8");
		return response;
	}
}
