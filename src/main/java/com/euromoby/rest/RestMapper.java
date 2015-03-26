package com.euromoby.rest;

import java.net.URI;

import com.euromoby.rest.handler.RestHandler;
import com.euromoby.rest.handler.cli.CliHandler;
import com.euromoby.rest.handler.job.JobAddHandler;
import com.euromoby.rest.handler.job.JobListHandler;
import com.euromoby.rest.handler.ping.PingHandler;
import com.euromoby.rest.handler.upload.UploadHandler;
import com.euromoby.rest.handler.welcome.WelcomeHandler;
import com.euromoby.rest.handler.whatismyip.WhatIsMyIpHandler;


public class RestMapper {

	public static RestHandler getHandler(URI uri) {
		if (uri.getPath().equals(WelcomeHandler.URL)) {
			return new WelcomeHandler();
		}
		if (uri.getPath().equals(WhatIsMyIpHandler.URL)) {
			return new WhatIsMyIpHandler();
		}
		if (uri.getPath().equals(CliHandler.URL)) {
			return new CliHandler();
		}
		if (uri.getPath().equals(UploadHandler.URL)) {
			return new UploadHandler();
		}
		if (uri.getPath().equals(PingHandler.URL)) {
			return new PingHandler();
		}
		if (uri.getPath().equals(JobListHandler.URL)) {
			return new JobListHandler();
		}
		if (uri.getPath().equals(JobAddHandler.URL)) {
			return new JobAddHandler();
		}

		return null;
	}

}
