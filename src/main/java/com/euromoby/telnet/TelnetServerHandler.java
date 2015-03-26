package com.euromoby.telnet;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.euromoby.agent.Agent;
import com.euromoby.processor.CommandProcessor;
import com.euromoby.utils.StringUtils;


@Sharable
public class TelnetServerHandler extends SimpleChannelInboundHandler<String> {

	private CommandProcessor commandProcessor;

	public TelnetServerHandler() {
		this.commandProcessor = Agent.get().getCommandProcessor();
	}

	private static final Logger LOG = LoggerFactory.getLogger(TelnetServerHandler.class); 	
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// Send greeting for a new connection.
		ctx.write("Agent on " + InetAddress.getLocalHost().getHostName() + StringUtils.CRLF);
		ctx.flush();
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, String request) throws Exception {
		// Generate and write a response.
		String response;
		boolean close = false;
		if (request.isEmpty()) {
			response = StringUtils.CRLF;
		} else if ("exit".equals(request.toLowerCase())) {
			response = "Bye!" + StringUtils.CRLF;
			close = true;
		} else {
			response = commandProcessor.process(request) + StringUtils.CRLF;
		}

		ChannelFuture future = ctx.write(response);

		if (close) {
			future.addListener(ChannelFutureListener.CLOSE);
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		LOG.debug("Exception", cause);
		ctx.close();
	}
}
