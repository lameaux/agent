package com.euromoby.processor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.job.JobDetail;
import com.euromoby.job.JobManager;
import com.euromoby.upload.UploadJob;
import com.euromoby.utils.StringUtils;

@Component
public class UploadCommand extends CommandBase implements Command {

	public static final String NAME = "upload";	
	public static final String NO_PROXY = "noproxy";

	private JobManager jobManager;
	
	@Autowired
	public UploadCommand(JobManager jobManager) {
		this.jobManager = jobManager;
	}
	
	@Override
	public String execute(String request) {
		String[] params = parameters(request);
		if (params.length < 2 || StringUtils.nullOrEmpty(params[0]) || StringUtils.nullOrEmpty(params[1])) {
			return syntaxError();
		}

		String location = params[0];
		String url = params[1];
		boolean noProxy = (params.length == 3 && NO_PROXY.equals(params[2]));

		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(UploadJob.PARAM_URL, url);
		parameters.put(UploadJob.PARAM_LOCATION, location);
		parameters.put(UploadJob.PARAM_NOPROXY, String.valueOf(noProxy));

		JobDetail jobDetail = new JobDetail(UploadJob.class, parameters);
		jobManager.submit(jobDetail);

		return jobDetail.toString();

	}

	@Override
	public String help() {
		
		return NAME + "\t<location> <agent-upload-url> [noproxy]\t\tupload file from <location> to <agent-upload-url>  (using configured proxy or directly)" + StringUtils.CRLF
				+ StringUtils.CRLF + 
				"Examples:" + StringUtils.CRLF + 
				NAME + "\tbundle.zip http://agent1:21080/upload\t\tuse proxy if available" + StringUtils.CRLF + 
				NAME + "\tbundle.zip http://agent1:21080/upload noproxy\tignore proxy configuration";

	}

	@Override
	public String name() {
		return NAME;
	}

}
