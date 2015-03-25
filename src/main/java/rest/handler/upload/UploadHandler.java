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

import rest.RestException;
import rest.handler.RestHandlerBase;
import utils.IOUtils;
import utils.StringUtils;
import agent.Agent;

public class UploadHandler extends RestHandlerBase {

	public static final String URL = "/upload";

	private static final String REQUEST_INPUT_LOCATION = "location";
	private static final String REQUEST_INPUT_FILE = "file";

	private String uploadPath;

	private static final Logger LOG = LoggerFactory.getLogger(UploadHandler.class);

	public UploadHandler() {
		this.uploadPath = Agent.get().getConfig().getAgentFilesPath();
	}

	@Override
	public FullHttpResponse doGet() {
		InputStream is = UploadHandler.class.getResourceAsStream("upload.html");
		String pageContent = IOUtils.streamToString(is);
		pageContent = pageContent.replace("%UPLOAD_PATH%", uploadPath);
		pageContent = pageContent.replace("%FILE_SEPARATOR%", File.separator);
		ByteBuf content = Unpooled.copiedBuffer(pageContent, CharsetUtil.UTF_8);
		return createHttpResponse(HttpResponseStatus.OK, content);
	}

	@Override
	public FullHttpResponse doPost() throws RestException, IOException {

		Map<String, String> requestParameters = getRequestParameters();
		Map<String, File> requestFiles = getRequestFiles();

		String location = requestParameters.get(REQUEST_INPUT_LOCATION);
		File tempUploadedFile = requestFiles.get(REQUEST_INPUT_FILE);

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

		return createHttpResponse(HttpResponseStatus.OK, fromString("OK"));
	}


}
