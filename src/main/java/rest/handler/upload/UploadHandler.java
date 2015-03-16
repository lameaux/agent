package rest.handler.upload;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.CharsetUtil;

import java.io.InputStream;

import rest.handler.RestHandlerBase;
import utils.IOUtils;

public class UploadHandler extends RestHandlerBase {

	// Get show input form
	@Override
	public FullHttpResponse doGet() {
		InputStream is = UploadHandler.class.getResourceAsStream("upload.html");
		return getFullHttpResponse(Unpooled.copiedBuffer(IOUtils.streamToString(is), CharsetUtil.UTF_8));
	}

	// POST
	@Override
	public FullHttpResponse doPost() {
		// CliRequest cliRequest = request.getBodyAs(CliRequest.class);
		// String message = commandProcessor.process(cliRequest.getRequest());
		// return new CliResponse(message);
		return getFullHttpResponse(Unpooled.copiedBuffer("POST", CharsetUtil.UTF_8));
	}
}
