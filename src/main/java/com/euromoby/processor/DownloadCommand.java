package com.euromoby.processor;

import java.util.HashMap;
import java.util.Map;

import com.euromoby.agent.Agent;
import com.euromoby.download.DownloadJob;
import com.euromoby.job.JobDetail;
import com.euromoby.job.JobManager;
import com.euromoby.utils.StringUtils;


public class DownloadCommand extends CommandBase implements Command {

	private static final String NO_PROXY = "noproxy";	
	
	@Override
	public String execute(String request) {
		String[] params = parameters(request);
		if (params.length < 2 || StringUtils.nullOrEmpty(params[0]) || StringUtils.nullOrEmpty(params[1])) {
			return syntaxError();
		}

		JobManager jobManager = Agent.get().getJobManager();		
		
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
		return "download\t<url> <location> [noproxy]\t\tdownload file from <url> to <location> using proxy" + StringUtils.CRLF + StringUtils.CRLF + 
				"Examples:" + StringUtils.CRLF + 
				"download\thttp://google.com google_page.html\t\tuse proxy if available" + StringUtils.CRLF +
				"download\thttp://google.com google_page.html noproxy\tignore proxy configuration";
	}	
	
	@Override
	public String name() {
		return "download";
	}

}
