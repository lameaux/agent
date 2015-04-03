package com.euromoby.processor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.download.DownloadJob;
import com.euromoby.job.JobDetail;
import com.euromoby.job.JobManager;
import com.euromoby.utils.StringUtils;

@Component
public class DownloadCommand extends CommandBase implements Command {

	public static final String NAME = "download";
	public static final String NO_PROXY = "noproxy";
	
	private JobManager jobManager;
	
	@Autowired
	public DownloadCommand(JobManager jobManager) {
		this.jobManager = jobManager;
	}
	
	@Override
	public String execute(String request) {
		String[] params = parameters(request);
		if (params.length < 2 || StringUtils.nullOrEmpty(params[0]) || StringUtils.nullOrEmpty(params[1])) {
			return syntaxError();
		}

		String url = params[0];
		String location = params[1];
		boolean noProxy = (params.length == 3 && NO_PROXY.equals(params[2]));

		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(DownloadJob.PARAM_URL, url);
		parameters.put(DownloadJob.PARAM_LOCATION, location);
		parameters.put(DownloadJob.PARAM_NOPROXY, String.valueOf(noProxy));

		JobDetail jobDetail = new JobDetail(DownloadJob.class, parameters);
		jobManager.submit(jobDetail);

		return jobDetail.toString();

	}

	@Override
	public String help() {
		return NAME + "\t<url> <location> [noproxy]\t\tdownload file from <url> to <location>  (using configured proxy or directly)" + StringUtils.CRLF + StringUtils.CRLF + "Examples:"
				+ StringUtils.CRLF + NAME + "\thttp://google.com google_page.html\t\tuse proxy if available" + StringUtils.CRLF
				+ NAME + "\thttp://google.com google_page.html noproxy\tignore proxy configuration";
	}

	@Override
	public String name() {
		return NAME;
	}

}
