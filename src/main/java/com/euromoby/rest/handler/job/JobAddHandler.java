package com.euromoby.rest.handler.job;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import java.io.InputStream;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import com.euromoby.agent.Agent;
import com.euromoby.job.JobDetail;
import com.euromoby.rest.RestException;
import com.euromoby.rest.handler.RestHandlerBase;
import com.euromoby.utils.DateUtils;
import com.euromoby.utils.IOUtils;
import com.euromoby.utils.StringUtils;


public class JobAddHandler extends RestHandlerBase {

	public static final String URL = "/jobs/add";

	private static final String REQUEST_INPUT_JOB_CLASS = "job_class";
	private static final String REQUEST_INPUT_SCHEDULE_TIME = "schedule_time";
	private static final String REQUEST_INPUT_PARAMETERS = "parameters";

	@Override
	public FullHttpResponse doGet() {
		InputStream is = JobAddHandler.class.getResourceAsStream("jobadd.html");
		String pageContent = IOUtils.streamToString(is);
		pageContent = pageContent.replace("%NOW%", DateUtils.iso(System.currentTimeMillis()));
		ByteBuf content = Unpooled.copiedBuffer(pageContent, CharsetUtil.UTF_8);
		return createHttpResponse(HttpResponseStatus.OK, content);
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

		Agent.get().getJobManager().submit(jobDetail);

		return createHttpResponse(HttpResponseStatus.OK, fromString("OK"));
	}

	private Map<String, String> parseParameters(String parameters) {
		Map<String, String> map = new HashMap<>();
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
