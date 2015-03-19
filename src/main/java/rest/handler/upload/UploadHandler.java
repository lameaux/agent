package rest.handler.upload;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import rest.handler.RestHandlerBase;
import utils.IOUtils;

public class UploadHandler extends RestHandlerBase {

	// GET show input form
	@Override
	public FullHttpResponse doGet() {
		InputStream is = UploadHandler.class.getResourceAsStream("upload.html");
		return getFullHttpResponse(Unpooled.copiedBuffer(IOUtils.streamToString(is), CharsetUtil.UTF_8));
	}

	// POST
	@Override
	public FullHttpResponse doPost() throws IOException {

		Map<String, String> requestParameters = getRequestParameters();
		Map<String, File> requestFiles = getRequestFiles();

		return getFullHttpResponse();
	}

	
	
}
