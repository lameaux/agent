package com.euromoby.rest;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseEncoder;

import com.euromoby.agent.Agent;

public class AgentHttpResponseEncoder extends HttpResponseEncoder {
    @Override
	protected void encodeHeaders(HttpHeaders headers, ByteBuf buf) {
		headers.set("X-Frame-Options", "SAMEORIGIN");
		headers.set("P3P", "CP=\"This site does not have a p3p policy.\"");
		headers.set(HttpHeaders.Names.SERVER, Agent.TITLE + " " + Agent.VERSION);		
        super.encodeHeaders(headers, buf);
    }	
}
