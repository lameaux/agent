package com.euromoby.mail;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.euromoby.agent.Agent;
import com.euromoby.mail.command.SmtpCommand;
import com.euromoby.mail.command.QuitSmtpCommand;
import com.euromoby.model.Tuple;
import com.euromoby.utils.StringUtils;

public class MailServerHandler extends SimpleChannelInboundHandler<String> {

	private SmtpCommandProcessor mailCommandProcessor;
	private MailSession mailSession;

	public MailServerHandler(SmtpCommandProcessor mailCommandProcessor) {
		this.mailCommandProcessor = mailCommandProcessor;
	}

	private static final Logger LOG = LoggerFactory.getLogger(MailServerHandler.class); 	
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		
		mailSession = new MailSession();
		
		// Send greeting for a new connection.
		String greeting = "220 " + Agent.TITLE + " " + Agent.VERSION + " SMTP Server is waiting for your HELO/EHLO ";
		ctx.writeAndFlush(greeting + StringUtils.CRLF);
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, String line) throws Exception {
		
		if (!mailSession.isCommandMode()) {
			// process message body
			return;
		}

		Tuple<String, String> request = Tuple.splitString(line, SmtpCommand.SEPARATOR);
		String command = request.getFirst().toUpperCase();
		
		String response = mailCommandProcessor.process(mailSession, request);
		ChannelFuture future = ctx.write(response + StringUtils.CRLF);

		if (QuitSmtpCommand.COMMAND_NAME.equals(command)) {
			future.addListener(ChannelFutureListener.CLOSE);
			mailSession = null;
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
