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

	private static final String REQUEST_INPUT_JOB_CLASS = "job_class";
	private static final String REQUEST_INPUT_SCHEDULE_TIME = "schedule_time";
	private static final String REQUEST_INPUT_PARAMETERS = "parameters";

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
	
	@Override
	public FullHttpResponse doGet() {
		InputStream is = JobAddHandler.class.getResourceAsStream("jobadd.html");
		String pageContent = IOUtils.streamToString(is);
		pageContent = pageContent.replace("%NOW%", DateUtils.iso(System.currentTimeMillis()));
		
		StringBuffer sb = new StringBuffer();

		for (@SuppressWarnings("rawtypes") Class jobClass : jobFactory.getJobClasses()) {
			sb.append("<option value=\"").append(jobClass.getCanonicalName()).append("\">")
			.append(jobClass.getSimpleName()).append("</option>").append(StringUtils.CRLF);
		}
		pageContent = pageContent.replace("%JOB_CLASSES%", sb.toString());
		
		ByteBuf content = Unpooled.copiedBuffer(pageContent, CharsetUtil.UTF_8);
		return httpResponseProvider.createHttpResponse(HttpResponseStatus.OK, content);
	}

	@Override
	public FullHttpResponse doPost() throws RestException {

		Map<String, String> requestParameters = getRequestParameters();

		String jobClass = requestParameters.get(REQUEST_INPUT_JOB_CLASS);
		String parameters = requestParameters.get(REQUEST_INPUT_PARAMETERS);
		String scheduleTimeString = requestParameters.get(REQUEST_INPUT_SCHEDULE_TIME);

		if (StringUtils.nullOrEmpty(jobClass)) {
			throw new RestException("Parameter is missing: " + REQUEST_INPUT_JOB_CLASS);
		}

		long scheduleTime = System.currentTimeMillis();
		if (!StringUtils.nullOrEmpty(scheduleTimeString)) {
			try {
				scheduleTime = DateUtils.fromIso(scheduleTimeString);
			} catch (ParseException e) {
				throw new RestException("Wrong format: " + REQUEST_INPUT_SCHEDULE_TIME);
			}
		}

		JobDetail jobDetail = new JobDetail();
		jobDetail.setJobClass(jobClass);
		jobDetail.setParameters(parseParameters(parameters));
		jobDetail.setScheduleTime(scheduleTime);

		jobManager.submit(jobDetail);

		return httpResponseProvider.createHttpResponse(HttpResponseStatus.OK, HttpUtils.fromString("OK"));
	}

	private Map<String, String> parseParameters(String parameters) {
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
