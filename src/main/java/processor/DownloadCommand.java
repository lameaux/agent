package processor;

import utils.StringUtils;
import download.Downloader;

public class DownloadCommand extends CommandBase implements Command {

	private static final String NO_PROXY = "noproxy";	
	
	public String execute(String request) {
		String[] params = parameters(request);
		if (params.length < 2 || StringUtils.nullOrEmpty(params[0]) || StringUtils.nullOrEmpty(params[1])) {
			return syntaxError();
		}

		String url = params[0];		
		String location = params[1];
		boolean noProxy = (params.length == 2 && NO_PROXY.equals(params[2]));
		
		Downloader downloader = new Downloader();
		try {
			downloader.download(url, location, noProxy);
			return "OK";
		} catch (Exception e) {
			return e.getMessage();
		}
		
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
