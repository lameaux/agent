package processor;

import job.JobManager;
import utils.StringUtils;
import agent.Agent;
import download.DownloadJob;

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
		
		DownloadJob downloadJob = new DownloadJob(url, location, noProxy);
		jobManager.submit(downloadJob);

		return downloadJob.description();
		
	}

	@Override
	public String help() {
		return "download url location [noproxy], Example: download http://google.com google_page.html";
	}	
	
	@Override
	public String name() {
		return "download";
	}

}
