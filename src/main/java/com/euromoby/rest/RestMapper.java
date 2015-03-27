package com.euromoby.rest;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.rest.handler.RestHandler;

@Component
public class RestMapper {

	private List<RestHandler> restHandlers;

	@Autowired
	public RestMapper(List<RestHandler> restHandlers) {
		this.restHandlers = restHandlers;
	}

	public RestHandler getHandler(URI uri) {
		for (RestHandler restHandler : restHandlers) {
			if (restHandler.matchUri(uri)) {
				return restHandler;
			}
		}
		return null;
	}

}
