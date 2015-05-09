package com.euromoby.rest.handler.job;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.http.HttpResponseProvider;
import com.euromoby.job.JobManager;
import com.euromoby.job.model.JobDetail;
import com.euromoby.job.model.JobState;
import com.euromoby.rest.handler.RestHandlerBase;
import com.euromoby.utils.DateUtils;
import com.euromoby.utils.IOUtils;

@Component
public class JobListHandler extends RestHandlerBase {

	public static final String URL = "/jobs";

	private JobManager jobManager;
	
	@Autowired
	public JobListHandler(JobManager jobManager) {
		this.jobManager = jobManager;
	}	
	
	@Override
	public boolean matchUri(URI uri) {
		return uri.getPath().equals(URL);
	}
	
	@Override
	public FullHttpResponse doGet(ChannelHandlerContext ctx, HttpRequest request, Map<String, List<String>> queryParameters) {
		InputStream is = JobListHandler.class.getResourceAsStream("joblist.html");
		String pageContent = IOUtils.streamToString(is);

		Set<JobDetail> jobs = jobManager.getSnapshot();
		StringBuffer sb = new StringBuffer();

		for (JobDetail job : jobs) {
			sb.append("<tr>");
			sb.append("<td>").append(DateUtils.iso(job.getScheduleTime())).append("</td>");
			sb.append("<td>").append(job.getJobClass()).append("</td>");
			sb.append("<td>").append(job.getState()).append("</td>");
			sb.append("<td>");
			if (job.getState() == JobState.FINISHED || job.getState() == JobState.FAILED) {
				sb.append("Finished: ").append(DateUtils.iso(job.getFinishTime()));
			}
			if (job.isError()) {
				sb.append("<br>").append("Error: ").append(job.getMessage());
			}
			sb.append("</td>");				
			sb.append("</tr>");
		}
		pageContent = pageContent.replace("%JOBS%", sb.toString());

		ByteBuf content = Unpooled.copiedBuffer(pageContent, CharsetUtil.UTF_8);
		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
		return httpResponseProvider.createHttpResponse(HttpResponseStatus.OK, content);
	}

}
