package com.euromoby.telnet;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.euromoby.processor.CommandProcessor;
import com.euromoby.processor.ExitCommand;
import com.euromoby.utils.StringUtils;

public class TelnetServerHandler extends SimpleChannelInboundHandler<String> {

	private CommandProcessor commandProcessor;

	public TelnetServerHandler(CommandProcessor commandProcessor) {
		this.commandProcessor = commandProcessor;
	}

	private static final Logger LOG = LoggerFactory.getLogger(TelnetServerHandler.class); 	
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ctx.writeAndFlush("Agent on " + InetAddress.getLocalHost().getHostName() + StringUtils.CRLF);
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, String request) throws Exception {
		String response = commandProcessor.process(request) + StringUtils.CRLF;
		ChannelFuture future = ctx.write(response);
		
		if (ExitCommand.NAME.equals(request.toLowerCase())) {
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
