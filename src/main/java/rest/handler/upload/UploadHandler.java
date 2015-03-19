package rest.handler.upload;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rest.handler.RestHandlerBase;
import utils.IOUtils;
import utils.StringUtils;

public class UploadHandler extends RestHandlerBase {

	public static final String URL = "/upload";
	
	private static final String REQUEST_INPUT_LOCATION = "location";
	private static final String REQUEST_INPUT_FILE = "file";
	
	private String uploadPath;
	
	private static final Logger LOG = LoggerFactory.getLogger(UploadHandler.class);
	
	public UploadHandler(String uploadPath) {
		this.uploadPath = uploadPath;
	}
	
	// GET show input form
	@Override
	public FullHttpResponse doGet() {
		InputStream is = UploadHandler.class.getResourceAsStream("upload.html");
		String pageContent = IOUtils.streamToString(is);
		pageContent = pageContent.replace("%UPLOAD_PATH%", uploadPath);
		pageContent = pageContent.replace("%FILE_SEPARATOR%", File.separator);
		ByteBuf content = Unpooled.copiedBuffer(pageContent, CharsetUtil.UTF_8);
		return createHttpResponse(HttpResponseStatus.OK, content);
	}

	// POST
	@Override
	public FullHttpResponse doPost() throws IOException {

		Map<String, String> requestParameters = getRequestParameters();
		Map<String, File> requestFiles = getRequestFiles();

		String location = requestParameters.get(REQUEST_INPUT_LOCATION);
		File tempUploadedFile = requestFiles.get(REQUEST_INPUT_FILE);
		
		if (StringUtils.nullOrEmpty(location)) {
			return redirectResponse(URL + "?error=location");
		}
		
		if (tempUploadedFile == null) {
			return redirectResponse(URL + "?error=file");
		}
		
		File targetFile = new File(new File(uploadPath), location);
		File parentDir = targetFile.getParentFile();
		if (!parentDir.exists() && !parentDir.mkdirs()) {
			return redirectResponse(URL + "?error=server");
		}
		
		FileUtils.copyFile(tempUploadedFile, targetFile);
		LOG.info("Uploaded file " + targetFile.getPath());
		
		return redirectResponse(URL + "?ok");
	}

	
	private FullHttpResponse redirectResponse(String location) {
		FullHttpResponse response = createHttpResponse(HttpResponseStatus.FOUND);
		response.headers().add("Location", location);
		return response;		
	}
	
	
}
