package rest.handler.job;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import job.JobDetail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rest.handler.RestHandlerBase;
import utils.IOUtils;
import utils.StringUtils;
import agent.Agent;

public class JobAddHandler extends RestHandlerBase {

	public static final String URL = "/jobadd";

	private static final String REQUEST_INPUT_JOB_CLASS = "job_class";
	private static final String REQUEST_INPUT_PARAMETERS = "parameters";
	
	private static final Logger LOG = LoggerFactory.getLogger(JobAddHandler.class);

	@Override
	public FullHttpResponse doGet() {
		InputStream is = JobAddHandler.class.getResourceAsStream("jobadd.html");
		String pageContent = IOUtils.streamToString(is);
		ByteBuf content = Unpooled.copiedBuffer(pageContent, CharsetUtil.UTF_8);
		return createHttpResponse(HttpResponseStatus.OK, content);
	}

	@Override
	public FullHttpResponse doPost() throws IOException {

		Map<String, String> requestParameters = getRequestParameters();

		String jobClass = requestParameters.get(REQUEST_INPUT_JOB_CLASS);
		String parameters = requestParameters.get(REQUEST_INPUT_PARAMETERS);
		
		if (StringUtils.nullOrEmpty(jobClass)) {
			return redirectResponse(URL + "?result=error_job_class");
		}

		if (StringUtils.nullOrEmpty(parameters)) {
			return redirectResponse(URL + "?result=error_parameters");
		}

		JobDetail jobDetail = new JobDetail();
		jobDetail.setJobClass(jobClass);
		jobDetail.setParameters(parseParameters(parameters));
		
		Agent.get().getJobManager().submit(jobDetail);

		return redirectResponse(URL + "?result=success");
	}

	private Map<String, String> parseParameters(String parameters) {
		Map<String, String> map = new HashMap<>();
		String[] lines = parameters.trim().split("\r\n");
		for (String line : lines) {
			String[] keyValue = line.split("=", 2);
			if (keyValue.length == 2) {
				map.put(keyValue[0], keyValue[1]);
			}
		}
		
		return map;
	}
	
	private FullHttpResponse redirectResponse(String location) {
		FullHttpResponse response = createHttpResponse(HttpResponseStatus.FOUND);
		response.headers().add("Location", location);
		return response;
	}

}
