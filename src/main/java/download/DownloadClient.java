package download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import agent.Agent;
import agent.Configuration;

public class DownloadClient {

	private static final Logger LOG = LoggerFactory.getLogger(DownloadClient.class);

	private Configuration config;

	public DownloadClient() {
		config = Agent.get().getConfig();
	}

	public void download(String url, String location, boolean noProxy) throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {

			RequestConfig.Builder requestConfigBuilder = RequestConfig.custom().setSocketTimeout(3000).setConnectTimeout(3000);

			if (!noProxy && config.isHttpProxy()) {
				requestConfigBuilder.setProxy(new HttpHost(config.getHttpProxyHost(), config.getHttpProxyPort()));
			}

			HttpGet request = new HttpGet(url);
			request.setConfig(requestConfigBuilder.build());

			CloseableHttpResponse response = httpclient.execute(request);
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
				throw new Exception(statusLine.getStatusCode() + " " + statusLine.getReasonPhrase());
			}
			
			try {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					InputStream inputStream = entity.getContent();
					File targetFile = getTargetFile(location);
					OutputStream outputStream = new FileOutputStream(targetFile);
					try {
						IOUtils.copy(inputStream, outputStream);
						LOG.debug("File saved to " + targetFile.getPath());
					} finally {
						IOUtils.closeQuietly(inputStream);
						IOUtils.closeQuietly(outputStream);
					}
				}
			} finally {
				response.close();
			}
		} finally {
			httpclient.close();
		}
	}

	private File getTargetFile(String location) throws Exception {
		File targetFile = new File(new File(config.getAgentFilesPath()), location);
		File parentDir = targetFile.getParentFile();
		if (!parentDir.exists() && !parentDir.mkdirs()) {
			throw new Exception("Error saving file to " + location);
		}
		return targetFile;
	}

}
