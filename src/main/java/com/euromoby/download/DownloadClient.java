package com.euromoby.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.http.HttpClientProvider;

@Component
public class DownloadClient {

	private static final Logger LOG = LoggerFactory.getLogger(DownloadClient.class);

	public static final String DOWNLOADING_EXT = ".downloading";
	
	private HttpClientProvider httpClientProvider;	

	@Autowired
	public DownloadClient(HttpClientProvider httpClientProvider) {
		this.httpClientProvider = httpClientProvider;
	}

	public void download(String url, File targetFile, boolean noProxy) throws Exception {
		CloseableHttpClient httpclient = httpClientProvider.createHttpClient();
		try {

			HttpGet request = new HttpGet(url);
			RequestConfig.Builder requestConfigBuilder = httpClientProvider.createRequestConfigBuilder(request.getURI().getHost(), noProxy);
			request.setConfig(requestConfigBuilder.build());

			CloseableHttpResponse response = httpclient.execute(request, httpClientProvider.createHttpClientContext());
			
			try {

				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
					EntityUtils.consumeQuietly(response.getEntity());					
					throw new Exception(statusLine.getStatusCode() + " " + statusLine.getReasonPhrase());
				}
				
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					InputStream inputStream = entity.getContent();
					File downloadingFile = new File(targetFile.getCanonicalPath() + DOWNLOADING_EXT);
					OutputStream outputStream = new FileOutputStream(downloadingFile);
					try {
						IOUtils.copy(inputStream, outputStream);
						IOUtils.closeQuietly(outputStream);
						FileUtils.moveFile(downloadingFile, targetFile);
						LOG.debug("File saved to " + targetFile.getPath());
					} finally {
						IOUtils.closeQuietly(inputStream);
						IOUtils.closeQuietly(outputStream);
						if (downloadingFile.exists()) {
							downloadingFile.delete();
						}
					}
				} else {
					throw new Exception("Empty response");
				}
			} finally {
				response.close();
			}
		} finally {
			httpclient.close();
		}
	}


}
