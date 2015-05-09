package com.euromoby.rest.handler.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.euromoby.job.Job;
import com.euromoby.job.JobFactory;
import com.euromoby.job.JobManager;
import com.euromoby.job.model.JobDetail;
import com.euromoby.rest.RestException;
import com.euromoby.utils.DateUtils;
import com.euromoby.utils.StringUtils;

@RunWith(MockitoJUnitRunner.class)
public class JobAddHandlerTest {

	@Mock
	JobManager jobManager;
	@Mock
	JobFactory jobFactory;
	@Mock
	HttpRequest request;
	@Mock
	HttpHeaders headers;
	@Mock
	ChannelHandlerContext ctx;
	
	
	JobAddHandler jobAddHandler;
	
	@Before
	public void init() {
		Mockito.when(request.getProtocolVersion()).thenReturn(HttpVersion.HTTP_1_1);
		Mockito.when(request.headers()).thenReturn(headers);
		jobAddHandler = new JobAddHandler(jobManager, jobFactory);
	}
	
	@Test
	public void shouldMatchUri() throws Exception {
		assertTrue(jobAddHandler.matchUri(new URI("http://example.com" + JobAddHandler.URL)));
	}

	@Test
	public void testParseParameters() {
		Map<String, String> map = jobAddHandler.parseParameters(null);
		assertTrue(map.isEmpty());

		String param1 = "foo";
		String value1 = "bar";
		String param2 = "lama";
		String parameters = param1 + "=" + value1 + StringUtils.CRLF + param1;		
		map = jobAddHandler.parseParameters(parameters);
		assertEquals(value1, map.get(param1));
		assertFalse(map.containsKey(param2));		
	}
	
	@Test
	public void testGetOptionListOfJobClasses() {
		Mockito.when(jobFactory.getJobClasses()).thenReturn(new Class[] {Job.class});
		String result = "<option value=\"com.euromoby.job.Job\">Job</option>";
		assertEquals(result, jobAddHandler.getOptionListOfJobClasses());
	}
	
	@Test
	public void testValidateRequestParameters() throws Exception {
		Map<String, List<String>> params = new HashMap<String, List<String>>();
		try {
			jobAddHandler.validateRequestParameters(params);
			fail();
		} catch (RestException e) {
			assertTrue(e.getMessage().contains(JobAddHandler.REQUEST_INPUT_JOB_CLASS));
		}
		
		params.put(JobAddHandler.REQUEST_INPUT_JOB_CLASS, Arrays.asList("foo"));
		jobAddHandler.validateRequestParameters(params);
		
	}
	
	@Test
	public void testScheduleTime() throws Exception {
		long time = jobAddHandler.getScheduleTime(null);
		assertTrue(time <= System.currentTimeMillis());
		
		try {
			jobAddHandler.getScheduleTime("foobar");
			fail();
		} catch (RestException e) {
			assertTrue(e.getMessage().contains(JobAddHandler.REQUEST_INPUT_SCHEDULE_TIME));
		}
	}
	
	@Test
	public void testGet() {
		Mockito.when(jobFactory.getJobClasses()).thenReturn(new Class[] {Job.class});
		FullHttpResponse response = jobAddHandler.doGet(ctx, request, null);
		assertEquals(HttpResponseStatus.OK, response.getStatus());
	}
	
	@Test
	public void testPost() throws Exception {
		
		String param1 = "foo";
		String value1 = "bar";
		String parameters = param1 + "=" + value1;
		long scheduleTime = 123000;
		
		Map<String, List<String>> params = new HashMap<String, List<String>>();
		params.put(JobAddHandler.REQUEST_INPUT_JOB_CLASS, Arrays.asList(Job.class.getCanonicalName()));
		params.put(JobAddHandler.REQUEST_INPUT_PARAMETERS, Arrays.asList(parameters));
		params.put(JobAddHandler.REQUEST_INPUT_SCHEDULE_TIME, Arrays.asList(DateUtils.iso(scheduleTime)));
		
		FullHttpResponse response = jobAddHandler.doPost(ctx, request, null, params, null);
		assertEquals(HttpResponseStatus.OK, response.getStatus());
		
		ArgumentCaptor<JobDetail> captor = ArgumentCaptor.forClass(JobDetail.class);
		Mockito.verify(jobManager).submit(captor.capture());
		JobDetail jobDetail = captor.getValue();
		assertEquals(Job.class.getCanonicalName(), jobDetail.getJobClass());
		assertEquals(value1, jobDetail.getParameters().get(param1));
		assertEquals(scheduleTime/1000, jobDetail.getScheduleTime()/1000);
	}
	
}
