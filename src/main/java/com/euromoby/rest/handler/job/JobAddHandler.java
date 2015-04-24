package com.euromoby.rest.handler.job;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import java.io.InputStream;
import java.net.URI;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.http.HttpResponseProvider;
import com.euromoby.http.HttpUtils;
import com.euromoby.job.JobDetail;
import com.euromoby.job.JobFactory;
import com.euromoby.job.JobManager;
import com.euromoby.rest.RestException;
import com.euromoby.rest.handler.RestHandlerBase;
import com.euromoby.utils.DateUtils;
import com.euromoby.utils.IOUtils;
import com.euromoby.utils.StringUtils;

@Component
public class JobAddHandler extends RestHandlerBase {

	public static final String URL = "/jobs/add";

	public static final String REQUEST_INPUT_JOB_CLASS = "job_class";
	public static final String REQUEST_INPUT_SCHEDULE_TIME = "schedule_time";
	public static final String REQUEST_INPUT_PARAMETERS = "parameters";

	private JobManager jobManager;
	private JobFactory jobFactory;
	
	@Autowired
	public JobAddHandler(JobManager jobManager, JobFactory jobFactory) {
		this.jobManager = jobManager;
		this.jobFactory = jobFactory;
	}
	
	@Override
	public boolean matchUri(URI uri) {
		return uri.getPath().equals(URL);
	}
	
	@SuppressWarnings("rawtypes")
	protected String getOptionListOfJobClasses() {
		StringBuffer sb = new StringBuffer();
		for (Class jobClass : jobFactory.getJobClasses()) {
			sb.append("<option value=\"").append(jobClass.getCanonicalName()).append("\">");
			sb.append(jobClass.getSimpleName());
			sb.append("</option>");
		}
		return sb.toString();
	}
	
	@Override
	public FullHttpResponse doGet() {
		InputStream is = JobAddHandler.class.getResourceAsStream("jobadd.html");
		String pageContent = IOUtils.streamToString(is);
		pageContent = pageContent.replace("%NOW%", DateUtils.iso(System.currentTimeMillis()));
		pageContent = pageContent.replace("%JOB_CLASSES%", getOptionListOfJobClasses());
		ByteBuf content = Unpooled.copiedBuffer(pageContent, CharsetUtil.UTF_8);
		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
		return httpResponseProvider.createHttpResponse(HttpResponseStatus.OK, content);
	}

	@Override
	public FullHttpResponse doPost() throws RestException {

		Map<String, String> requestParameters = getRequestParameters();

		validateRequestParameters(requestParameters);

		JobDetail jobDetail = new JobDetail();
		jobDetail.setJobClass(requestParameters.get(REQUEST_INPUT_JOB_CLASS));
		jobDetail.setParameters(parseParameters(requestParameters.get(REQUEST_INPUT_PARAMETERS)));
		jobDetail.setScheduleTime(getScheduleTime(requestParameters.get(REQUEST_INPUT_SCHEDULE_TIME)));

		jobManager.submit(jobDetail);

		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
		return httpResponseProvider.createHttpResponse(HttpResponseStatus.OK, HttpUtils.fromString("OK"));
	}

	protected void validateRequestParameters(Map<String, String> requestParameters) throws RestException {
		if (StringUtils.nullOrEmpty(requestParameters.get(REQUEST_INPUT_JOB_CLASS))) {
			throw new RestException("Parameter is missing: " + REQUEST_INPUT_JOB_CLASS);
		}
	}
	
	protected long getScheduleTime(String scheduleTimeString) throws RestException {
		long scheduleTime = System.currentTimeMillis();
		if (!StringUtils.nullOrEmpty(scheduleTimeString)) {
			try {
				scheduleTime = DateUtils.fromIso(scheduleTimeString);
			} catch (ParseException e) {
				throw new RestException("Wrong format: " + REQUEST_INPUT_SCHEDULE_TIME);
			}
		}	
		return scheduleTime;
	}
	
	protected Map<String, String> parseParameters(String parameters) {
		Map<String, String> map = new HashMap<String, String>();
		if (parameters == null) {
			return map;
		}
		String[] lines = parameters.trim().split(StringUtils.CRLF);
		for (String line : lines) {
			String[] keyValue = line.trim().split("=", 2);
			if (keyValue.length == 2) {
				map.put(keyValue[0].trim(), keyValue[1].trim());
			}
		}
		return map;
	}

}
