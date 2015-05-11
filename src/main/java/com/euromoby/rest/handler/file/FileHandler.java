package com.euromoby.rest.handler.file;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

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

import com.euromoby.file.FileProvider;
import com.euromoby.file.MimeHelper;
import com.euromoby.http.FileResponse;
import com.euromoby.http.HttpResponseProvider;
import com.euromoby.http.HttpUtils;
import com.euromoby.rest.RestException;
import com.euromoby.rest.handler.RestHandlerBase;

@Component
public class FileHandler extends RestHandlerBase {

	public static final String URL = "/files";
	private static final Pattern URL_PATTERN = Pattern.compile(URL + "/(.+)");

	private FileProvider fileProvider;
	private MimeHelper mimeHelper;

	@Autowired
	public FileHandler(FileProvider fileProvider, MimeHelper mimeHelper) {
		this.fileProvider = fileProvider;
		this.mimeHelper = mimeHelper;
	}

	@Override
	public boolean matchUri(URI uri) {
		Matcher m = URL_PATTERN.matcher(uri.getPath());
		return m.matches();
	}

	@Override
	public boolean isChunkedResponse() {
		return true;
	}
	
	@Override
	public void doGetChunked(ChannelHandlerContext ctx, HttpRequest request, Map<String, List<String>> queryParameters) throws Exception {
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
		if (targetFile == null || !targetFile.exists()) {
			throw new RestException(HttpResponseStatus.NOT_FOUND, "Not found");
		}
		
        // Cache Validation
		if (!HttpUtils.ifModifiedSince(request, targetFile)) {
			HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
			FullHttpResponse response = httpResponseProvider.createNotModifiedResponse();
			httpResponseProvider.writeResponse(ctx, response);
        	return;			
		}
		
		FileResponse fileResponse = new FileResponse(request, mimeHelper);
		fileResponse.send(ctx, targetFile);
	}

}
