package com.euromoby.cdn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.io.File;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.euromoby.download.DownloadJob;
import com.euromoby.file.FileProvider;
import com.euromoby.file.MimeHelper;
import com.euromoby.http.HttpUtils;
import com.euromoby.job.JobDetail;
import com.euromoby.job.JobManager;
import com.euromoby.model.AgentId;
import com.euromoby.model.Tuple;
import com.euromoby.rest.ChunkedInputAdapter;
import com.euromoby.rest.handler.fileinfo.FileInfo;

@RunWith(MockitoJUnitRunner.class)
public class CdnServerHandlerTest {

	private static final String INVALID_URI = "$[level]/r$[y]_c$[x].jpg";

	@Mock
	FileProvider fileProvider;
	@Mock
	MimeHelper mimeHelper;
	@Mock
	CdnNetwork cdnNetwork;
	@Mock
	JobManager jobManager;
	@Mock
	ChannelHandlerContext ctx;
	@Mock
	Channel channel;
	@Mock
	FullHttpRequest request;
	@Mock
	HttpHeaders headers;
	@Mock
	ChannelFuture channelFuture;
	@Mock
	ChannelPipeline channelPipeline;
	@Mock
	File targetFile;

	CdnServerHandler handler;

	@Before
	public void init() {
		Mockito.when(ctx.channel()).thenReturn(channel);
		Mockito.when(request.headers()).thenReturn(headers);
		Mockito.when(request.getProtocolVersion()).thenReturn(HttpVersion.HTTP_1_1);
		handler = new CdnServerHandler(fileProvider, mimeHelper, cdnNetwork, jobManager);
	}

	
	@Test
	public void testGetPathWithQuery() throws Exception {
		String PATH = "/path";
		URI uri = new URI("http://example.com" + PATH);
		assertEquals(PATH, handler.getPathWithQuery(uri));

		String PATH_WITH_QUERY = "/path?query=query";
		URI uriWithPath = new URI("http://example.com" + PATH_WITH_QUERY);
		assertEquals(PATH_WITH_QUERY, handler.getPathWithQuery(uriWithPath));
	}
	
	@Test
	public void testBuildAgentUrlLocation() {
		String URL_PATH = "/path";
		String HOST = "agent1";
		int PORT = 21000;
		AgentId agentId = new AgentId(HOST, PORT);
		String AGENT_URL = "http://" + HOST + ":" + (PORT + CdnServer.CDN_PORT) + URL_PATH;
		
		assertEquals(AGENT_URL, handler.buildAgentUrlLocation(agentId, URL_PATH));
	}
	
	@Test
	public void testExceptionCaught() throws Exception {
		handler.exceptionCaught(ctx, new Exception());
		Mockito.verify(channel).close();
	}

	@Test
	public void testSend100Continue() {
		handler.send100Continue(ctx);
		ArgumentCaptor<FullHttpResponse> captor = ArgumentCaptor.forClass(FullHttpResponse.class);
		Mockito.verify(ctx).write(captor.capture());
		FullHttpResponse response = captor.getValue();
		assertEquals(HttpResponseStatus.CONTINUE, response.getStatus());
	}	
	
	@Test
	public void testManageFileResponseNotFound() {
		Mockito.when(channel.writeAndFlush(Matchers.any(FullHttpResponse.class))).thenReturn(channelFuture);
		handler.manageFileResponse(ctx, request, targetFile);
		ArgumentCaptor<FullHttpResponse> captor = ArgumentCaptor.forClass(FullHttpResponse.class);
		Mockito.verify(channel).writeAndFlush(captor.capture());
		FullHttpResponse response = captor.getValue();
		assertEquals(HttpResponseStatus.NOT_FOUND, response.getStatus());
	}

	@Test
	public void testManageFileResponseOK() throws Exception {
		File tmpFile = File.createTempFile("foo", "bar");
		tmpFile.deleteOnExit();
		Mockito.when(ctx.writeAndFlush(Matchers.any(DefaultLastHttpContent.class))).thenReturn(channelFuture);
		Mockito.when(channel.pipeline()).thenReturn(channelPipeline);
		handler.manageFileResponse(ctx, request, tmpFile);
		ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
		Mockito.verify(ctx, Mockito.times(2)).write(captor.capture());
		List<Object> responseParts= captor.getAllValues();
		HttpResponse response = (HttpResponse)responseParts.get(0);
		assertEquals(HttpResponseStatus.OK, response.getStatus());
		assertTrue(responseParts.get(1) instanceof ChunkedInputAdapter);
	}

	
	
	@Test
	public void testInvalidHttpMethod() throws Exception {
		Mockito.when(request.getMethod()).thenReturn(HttpMethod.POST);
		Mockito.when(channel.writeAndFlush(Matchers.any(DefaultFullHttpResponse.class))).thenReturn(channelFuture);
		ArgumentCaptor<DefaultFullHttpResponse> responseCaptor = ArgumentCaptor.forClass(DefaultFullHttpResponse.class);
		handler.channelRead0(ctx, request);
		Mockito.verify(channel).writeAndFlush(responseCaptor.capture());
		FullHttpResponse response = responseCaptor.getValue();
		assertEquals(HttpResponseStatus.NOT_IMPLEMENTED, response.getStatus());
	}

	@Test
	public void testInvalidUri() throws Exception {
		Mockito.when(request.getMethod()).thenReturn(HttpMethod.GET);
		Mockito.when(request.getUri()).thenReturn(INVALID_URI);
		Mockito.when(channel.writeAndFlush(Matchers.any(DefaultFullHttpResponse.class))).thenReturn(channelFuture);
		ArgumentCaptor<DefaultFullHttpResponse> responseCaptor = ArgumentCaptor.forClass(DefaultFullHttpResponse.class);
		handler.channelRead0(ctx, request);
		Mockito.verify(channel).writeAndFlush(responseCaptor.capture());
		FullHttpResponse response = responseCaptor.getValue();
		assertEquals(HttpResponseStatus.BAD_REQUEST, response.getStatus());
	}

	@Test
	public void testEmptyFileLocation() throws Exception {
		Mockito.when(request.getMethod()).thenReturn(HttpMethod.GET);
		Mockito.when(request.getUri()).thenReturn("");
		Mockito.when(channel.writeAndFlush(Matchers.any(DefaultFullHttpResponse.class))).thenReturn(channelFuture);
		ArgumentCaptor<DefaultFullHttpResponse> responseCaptor = ArgumentCaptor.forClass(DefaultFullHttpResponse.class);
		handler.channelRead0(ctx, request);
		Mockito.verify(channel).writeAndFlush(responseCaptor.capture());
		FullHttpResponse response = responseCaptor.getValue();
		assertEquals(HttpResponseStatus.BAD_REQUEST, response.getStatus());
	}

	@Test
	public void testWrongFileLocation() throws Exception {
		Mockito.when(request.getMethod()).thenReturn(HttpMethod.GET);
		Mockito.when(request.getUri()).thenReturn("foo");
		Mockito.when(channel.writeAndFlush(Matchers.any(DefaultFullHttpResponse.class))).thenReturn(channelFuture);
		ArgumentCaptor<DefaultFullHttpResponse> responseCaptor = ArgumentCaptor.forClass(DefaultFullHttpResponse.class);
		handler.channelRead0(ctx, request);
		Mockito.verify(channel).writeAndFlush(responseCaptor.capture());
		FullHttpResponse response = responseCaptor.getValue();
		assertEquals(HttpResponseStatus.BAD_REQUEST, response.getStatus());
	}

	@Test
	public void testCacheNotModified() throws Exception {
		String FILE = "file";
		Mockito.when(request.getMethod()).thenReturn(HttpMethod.GET);
		Mockito.when(request.getUri()).thenReturn("/" + FILE);
		Mockito.when(channel.writeAndFlush(Matchers.any(DefaultFullHttpResponse.class))).thenReturn(channelFuture);
		long lastModified = System.currentTimeMillis();
		Mockito.when(targetFile.lastModified()).thenReturn(lastModified);
		Mockito.when(headers.get(Matchers.eq(HttpHeaders.Names.IF_MODIFIED_SINCE))).thenReturn(
				new SimpleDateFormat(HttpUtils.HTTP_DATE_FORMAT, Locale.US).format(new Date(lastModified)));
		Mockito.when(fileProvider.getFileByLocation(Matchers.eq(FILE))).thenReturn(targetFile);

		handler.channelRead0(ctx, request);
		ArgumentCaptor<DefaultFullHttpResponse> responseCaptor = ArgumentCaptor.forClass(DefaultFullHttpResponse.class);
		Mockito.verify(channel).writeAndFlush(responseCaptor.capture());
		FullHttpResponse response = responseCaptor.getValue();
		assertEquals(HttpResponseStatus.NOT_MODIFIED, response.getStatus());
	}

	@Test
	public void testSendFileNotCachedWithContinue() throws Exception {
		String FILE = "file";
		File tmpFile = File.createTempFile("foo", "bar");
		tmpFile.deleteOnExit();
		Mockito.when(ctx.writeAndFlush(Matchers.any(DefaultLastHttpContent.class))).thenReturn(channelFuture);
		Mockito.when(channel.pipeline()).thenReturn(channelPipeline);		
		Mockito.when(request.getMethod()).thenReturn(HttpMethod.GET);
		Mockito.when(headers.get(Matchers.refEq(HttpHeaders.newEntity(HttpHeaders.Names.EXPECT)))).thenReturn(HttpHeaders.Values.CONTINUE);		
		Mockito.when(request.getUri()).thenReturn("/" + FILE);
		Mockito.when(headers.get(Matchers.eq(HttpHeaders.Names.IF_MODIFIED_SINCE))).thenReturn(null);
		Mockito.when(fileProvider.getFileByLocation(Matchers.eq(FILE))).thenReturn(tmpFile);

		handler.channelRead0(ctx, request);
		
		ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
		Mockito.verify(ctx, Mockito.times(3)).write(captor.capture());
		List<Object> responseParts= captor.getAllValues();
		HttpResponse continueResponse = (HttpResponse)responseParts.get(0);
		assertEquals(HttpResponseStatus.CONTINUE, continueResponse.getStatus());
		
		HttpResponse response = (HttpResponse)responseParts.get(1);		
		assertEquals(HttpResponseStatus.OK, response.getStatus());
		assertTrue(responseParts.get(2) instanceof ChunkedInputAdapter);		
	}

	@Test
	public void testGetOriginRedirectFromCdn() throws Exception {
		String FILE = "file.html";
		String ORIGIN_URL = "http://example.com";
		String ORIGIN_FILE_URL = ORIGIN_URL + "/" + FILE + "?query=query";
		URI uri = new URI(ORIGIN_FILE_URL);
		Tuple<CdnResource, FileInfo> searchResult = Tuple.empty();
		CdnResource cdnResource = new CdnResource();
		cdnResource.setResourceOrigin(ORIGIN_URL);
		cdnResource.setProxyable(false); // do redirect
		searchResult.setFirst(cdnResource);
		Mockito.when(cdnNetwork.find(uri.getPath())).thenReturn(searchResult);
		
		Mockito.when(ctx.writeAndFlush(Matchers.any(DefaultLastHttpContent.class))).thenReturn(channelFuture);
		Mockito.when(channel.pipeline()).thenReturn(channelPipeline);		
		Mockito.when(request.getMethod()).thenReturn(HttpMethod.GET);
		Mockito.when(headers.get(Matchers.refEq(HttpHeaders.newEntity(HttpHeaders.Names.EXPECT)))).thenReturn(HttpHeaders.Values.CONTINUE);		
		Mockito.when(request.getUri()).thenReturn(ORIGIN_FILE_URL);
		Mockito.when(headers.get(Matchers.eq(HttpHeaders.Names.IF_MODIFIED_SINCE))).thenReturn(null);
		Mockito.when(fileProvider.getFileByLocation(Matchers.eq(FILE))).thenReturn(null);

		handler.channelRead0(ctx, request);
		
		ArgumentCaptor<DefaultFullHttpResponse> responseCaptor = ArgumentCaptor.forClass(DefaultFullHttpResponse.class);
		Mockito.verify(channel).writeAndFlush(responseCaptor.capture());
		FullHttpResponse response = responseCaptor.getValue();
		assertEquals(HttpResponseStatus.FOUND, response.getStatus());
		assertEquals(ORIGIN_FILE_URL, response.headers().get(HttpHeaders.Names.LOCATION));		
	}	

	
	@Test
	public void testManageRedirect() {
		String SOURCE_URL = "http://example.com/file.html";
		
		Mockito.when(channel.writeAndFlush(Matchers.any(DefaultFullHttpResponse.class))).thenReturn(channelFuture);

		handler.manageRedirect(ctx, request, SOURCE_URL);
		
		ArgumentCaptor<DefaultFullHttpResponse> responseCaptor = ArgumentCaptor.forClass(DefaultFullHttpResponse.class);
		Mockito.verify(channel).writeAndFlush(responseCaptor.capture());
		FullHttpResponse response = responseCaptor.getValue();
		assertEquals(HttpResponseStatus.FOUND, response.getStatus());
		assertEquals(SOURCE_URL, response.headers().get(HttpHeaders.Names.LOCATION));		
	}
	
	@Test
	public void testScheduleDownloadJob() {
		String FILE = "file.html";
		String SOURCE_URL = "http://example.com/" + FILE;
		
		handler.scheduleDownloadJob(SOURCE_URL, FILE);
		ArgumentCaptor<JobDetail> captor = ArgumentCaptor.forClass(JobDetail.class);		
		Mockito.verify(jobManager).submit(captor.capture());
		JobDetail jobDetail = captor.getValue();
		assertEquals(DownloadJob.class.getCanonicalName(), jobDetail.getJobClass());
		Map<String, String> params = jobDetail.getParameters();
		assertEquals(SOURCE_URL, params.get(DownloadJob.PARAM_URL));
		assertEquals(FILE, params.get(DownloadJob.PARAM_LOCATION));		
	}
	
	@Test
	public void testManageContentProxying() {
		String SOURCE_URL = "http://example.com/file.html";		
		
		Mockito.when(channel.writeAndFlush(Matchers.any(DefaultFullHttpResponse.class))).thenReturn(channelFuture);
		handler.manageContentProxying(ctx, request, SOURCE_URL);
		
		ArgumentCaptor<DefaultFullHttpResponse> responseCaptor = ArgumentCaptor.forClass(DefaultFullHttpResponse.class);
		Mockito.verify(channel).writeAndFlush(responseCaptor.capture());
		FullHttpResponse response = responseCaptor.getValue();
		assertEquals(HttpResponseStatus.GATEWAY_TIMEOUT, response.getStatus());
	}
	
	@Test
	public void testNotExistInCdn() throws Exception {
		String FILE = "file.html";
		URI uri = new URI("http://example.com/file.html");
		Tuple<CdnResource, FileInfo> searchResult = Tuple.empty();
		Mockito.when(cdnNetwork.find(uri.getPath())).thenReturn(searchResult);
		Mockito.when(channel.writeAndFlush(Matchers.any(DefaultFullHttpResponse.class))).thenReturn(channelFuture);

		handler.manageCdnRequest(ctx, request, uri, FILE);

		ArgumentCaptor<DefaultFullHttpResponse> responseCaptor = ArgumentCaptor.forClass(DefaultFullHttpResponse.class);
		Mockito.verify(channel).writeAndFlush(responseCaptor.capture());
		FullHttpResponse response = responseCaptor.getValue();
		assertEquals(HttpResponseStatus.NOT_FOUND, response.getStatus());
	}

	@Test
	public void testFoundInNetwork() throws Exception {
		String FILE = "file.html";
		URI uri = new URI("http://example.com" + "/" + FILE);
		Tuple<CdnResource, FileInfo> searchResult = Tuple.empty();
		searchResult.setFirst(new CdnResource());
		FileInfo fileInfo = new FileInfo();
		AgentId agentId = new AgentId("agent1:21000");
		fileInfo.setAgentId(agentId);
		searchResult.setSecond(fileInfo);
		Mockito.when(cdnNetwork.find(uri.getPath())).thenReturn(searchResult);
		Mockito.when(channel.writeAndFlush(Matchers.any(DefaultFullHttpResponse.class))).thenReturn(channelFuture);
		Mockito.when(headers.contains(Matchers.eq(HttpHeaders.Names.CONNECTION), Matchers.eq(HttpHeaders.Values.CLOSE), Matchers.eq(true))).thenReturn(true);

		handler.manageCdnRequest(ctx, request, uri, FILE);
		
		ArgumentCaptor<DefaultFullHttpResponse> responseCaptor = ArgumentCaptor.forClass(DefaultFullHttpResponse.class);
		Mockito.verify(channel).writeAndFlush(responseCaptor.capture());
		FullHttpResponse response = responseCaptor.getValue();
		assertEquals(HttpResponseStatus.FOUND, response.getStatus());
		String agentUrl = String.format("http://%s:%d%s", agentId.getHost(), agentId.getBasePort() + CdnServer.CDN_PORT, uri.getPath());
		assertEquals(agentUrl, response.headers().get(HttpHeaders.Names.LOCATION));
	}

	@Test
	public void testExistInCdnButNotFoundInNetwork() throws Exception {
		String FILE = "file.html";
		URI uri = new URI("http://example.com" + "/" + FILE);
		Tuple<CdnResource, FileInfo> searchResult = Tuple.empty();
		searchResult.setFirst(new CdnResource());

		Mockito.when(cdnNetwork.find(uri.getPath())).thenReturn(searchResult);
		Mockito.when(channel.writeAndFlush(Matchers.any(DefaultFullHttpResponse.class))).thenReturn(channelFuture);
		Mockito.when(headers.contains(Matchers.eq(HttpHeaders.Names.CONNECTION), Matchers.eq(HttpHeaders.Values.CLOSE), Matchers.eq(true))).thenReturn(true);

		handler.manageCdnRequest(ctx, request, uri, FILE);

		ArgumentCaptor<DefaultFullHttpResponse> responseCaptor = ArgumentCaptor.forClass(DefaultFullHttpResponse.class);
		Mockito.verify(channel).writeAndFlush(responseCaptor.capture());
		FullHttpResponse response = responseCaptor.getValue();
		assertEquals(HttpResponseStatus.NOT_FOUND, response.getStatus());
	}	
	
	@Test
	public void testNotFoundInNetworkRedirectToOrigin() throws Exception {
		String FILE = "file.html";
		String ORIGIN_URL = "http://example.com";
		String ORIGIN_FILE_URL = ORIGIN_URL + "/" + FILE + "?query=query";
		URI uri = new URI(ORIGIN_FILE_URL);
		Tuple<CdnResource, FileInfo> searchResult = Tuple.empty();
		CdnResource cdnResource = new CdnResource();
		cdnResource.setResourceOrigin(ORIGIN_URL);
		cdnResource.setProxyable(false); // do redirect
		searchResult.setFirst(cdnResource);
		Mockito.when(cdnNetwork.find(uri.getPath())).thenReturn(searchResult);
		Mockito.when(channel.writeAndFlush(Matchers.any(DefaultFullHttpResponse.class))).thenReturn(channelFuture);
		Mockito.when(headers.contains(Matchers.eq(HttpHeaders.Names.CONNECTION), Matchers.eq(HttpHeaders.Values.CLOSE), Matchers.eq(true))).thenReturn(true);

		handler.manageCdnRequest(ctx, request, uri, FILE);
		
		ArgumentCaptor<DefaultFullHttpResponse> responseCaptor = ArgumentCaptor.forClass(DefaultFullHttpResponse.class);
		Mockito.verify(channel).writeAndFlush(responseCaptor.capture());
		FullHttpResponse response = responseCaptor.getValue();
		assertEquals(HttpResponseStatus.FOUND, response.getStatus());
		assertEquals(ORIGIN_FILE_URL, response.headers().get(HttpHeaders.Names.LOCATION));
	}
	

	@Test
	public void testNotFoundInNetworkProxyContentAndDownload() throws Exception {
		String FILE = "file.html";
		String ORIGIN_URL = "http://example.com";
		String ORIGIN_FILE_URL = ORIGIN_URL + "/" + FILE + "?query=query";
		URI uri = new URI(ORIGIN_FILE_URL);
		Tuple<CdnResource, FileInfo> searchResult = Tuple.empty();
		CdnResource cdnResource = new CdnResource();
		cdnResource.setResourceOrigin(ORIGIN_URL);
		cdnResource.setProxyable(true); // do redirect
		cdnResource.setDownloadIfMissing(true); // create download job
		searchResult.setFirst(cdnResource);
		Mockito.when(cdnNetwork.find(uri.getPath())).thenReturn(searchResult);
		Mockito.when(channel.writeAndFlush(Matchers.any(DefaultFullHttpResponse.class))).thenReturn(channelFuture);
		Mockito.when(headers.contains(Matchers.eq(HttpHeaders.Names.CONNECTION), Matchers.eq(HttpHeaders.Values.CLOSE), Matchers.eq(true))).thenReturn(true);

		handler.manageCdnRequest(ctx, request, uri, FILE);
		
		Mockito.verify(jobManager).submit(Matchers.any(JobDetail.class));
		
		ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
		Mockito.verify(channel, Mockito.atLeastOnce()).writeAndFlush(captor.capture());
		List<Object> responseParts = captor.getAllValues();
		
		HttpResponse response = (HttpResponse)responseParts.get(0);		
		assertNotEquals(HttpResponseStatus.FOUND, response.getStatus());
	}	
	
	
}
