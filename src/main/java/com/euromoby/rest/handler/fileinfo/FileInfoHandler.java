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
import com.euromoby.file.FileProvider;
import com.euromoby.http.HttpResponseProvider;
import com.euromoby.rest.RestException;
import com.euromoby.rest.handler.RestHandlerBase;
import com.google.gson.Gson;

@Component
public class FileInfoHandler extends RestHandlerBase {

	public static final String URL = "/filesinfo";
	private static final Pattern URL_PATTERN = Pattern.compile(URL + "/(.+)");
	private static final Gson gson = new Gson();
	
	private Config config;
	private FileProvider fileProvider;

	@Autowired
	public FileInfoHandler(Config config, FileProvider fileProvider) {
		this.config = config;
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
		
		File targetFile = fileProvider.getFileByLocation(fileLocation);
		if (targetFile == null) {
			throw new RestException(HttpResponseStatus.NOT_FOUND, "Not found");
		}
		
		FileInfo fileInfoResponse = new FileInfo();
		fileInfoResponse.setAgentId(config.getAgentId());
		fileInfoResponse.setLength(targetFile.length());
		fileInfoResponse.setLastModified(targetFile.lastModified());
		
		String jsonResponse = gson.toJson(fileInfoResponse);
		ByteBuf content = Unpooled.copiedBuffer(jsonResponse, CharsetUtil.UTF_8);
		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
		FullHttpResponse response = httpResponseProvider.createHttpResponse(HttpResponseStatus.OK, content);
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json; charset=UTF-8");
		return response;
	}
}
