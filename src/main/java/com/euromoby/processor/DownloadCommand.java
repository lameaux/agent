package com.euromoby.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.download.DownloadFile;
import com.euromoby.download.DownloadManager;
import com.euromoby.utils.StringUtils;

@Component
public class DownloadCommand extends CommandBase implements Command {

	public static final String NAME = "download";
	public static final String NO_PROXY = "noproxy";
	
	public static final String DOWNLOAD_SCHEDULED = "Download scheduled #";
	
	private DownloadManager downloadManager;
	
	@Autowired
	public DownloadCommand(DownloadManager downloadManager) {
		this.downloadManager = downloadManager;
	}
	
	@Override
	public String execute(String request) {
		String[] params = parameters(request);
		if (params.length < 2 || StringUtils.nullOrEmpty(params[0]) || StringUtils.nullOrEmpty(params[1])) {
			return syntaxError();
		}

		String url = params[0];
		String fileLocation = params[1];
		boolean noProxy = (params.length == 3 && NO_PROXY.equals(params[2]));

		DownloadFile downloadFile = downloadManager.scheduleDownloadFile(url, fileLocation, noProxy);
		return DOWNLOAD_SCHEDULED + downloadFile.getId();
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
