package com.euromoby.upload;

import java.io.File;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.Config;
import com.euromoby.http.HttpClientProvider;

@Component
public class UploadClient {

	private static final String REQUEST_INPUT_LOCATION = "location";
	private static final String REQUEST_INPUT_FILE = "file";

	private Config config;
	private HttpClientProvider httpClientProvider;	

	@Autowired
	public UploadClient(Config config, HttpClientProvider httpClientProvider) {
		this.config = config;
		this.httpClientProvider = httpClientProvider;
	}

	public void upload(String location, String targetUrl, boolean noProxy) throws Exception {

			HttpPost request = new HttpPost(targetUrl);			
			RequestConfig.Builder requestConfigBuilder = httpClientProvider.createRequestConfigBuilder(request.getURI().getHost(), noProxy);
			request.setConfig(requestConfigBuilder.build());

			HttpEntity requestMultipartEntity = MultipartEntityBuilder.create().addTextBody(REQUEST_INPUT_LOCATION, location)
					.addBinaryBody(REQUEST_INPUT_FILE, getLocalFile(location)).build();
			request.setEntity(requestMultipartEntity);

			CloseableHttpResponse response = httpClientProvider.executeRequest(request);
			try {
				StatusLine statusLine = response.getStatusLine();
				EntityUtils.consumeQuietly(response.getEntity());
				if (statusLine.getStatusCode() / 100 != 2) {
					throw new Exception(statusLine.getStatusCode() + " " + statusLine.getReasonPhrase());
				}
			} finally {
				response.close();
			}
	}

	private File getLocalFile(String location) throws Exception {
		File targetFile = new File(new File(config.getAgentFilesPath()), location);
		if (!targetFile.exists() || targetFile.isDirectory()) {
			throw new Exception("Could not find " + targetFile.getAbsolutePath());
		}
		return targetFile;
	}

}
