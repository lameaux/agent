package com.euromoby.mail;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.euromoby.agent.Agent;
import com.euromoby.agent.Config;
import com.euromoby.mail.command.QuitSmtpCommand;
import com.euromoby.mail.command.SmtpCommand;
import com.euromoby.mail.util.DSNStatus;
import com.euromoby.model.Tuple;
import com.euromoby.utils.StringUtils;

public class MailServerHandler extends SimpleChannelInboundHandler<String> {

	private Config config;	
	private SmtpCommandProcessor mailCommandProcessor;
	private MailManager mailManager;
	
	private MailSession mailSession;

	public MailServerHandler(Config config, SmtpCommandProcessor mailCommandProcessor, MailManager mailManager) {
		this.config = config;
		this.mailCommandProcessor = mailCommandProcessor;
		this.mailManager = mailManager;
	}

	private static final Logger LOG = LoggerFactory.getLogger(MailServerHandler.class);

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		mailSession = new MailSession();
		String greeting = "220 " + config.getAgentId().getHost() + " ESMTP " + Agent.TITLE + " " + Agent.VERSION;
		ctx.writeAndFlush(greeting + StringUtils.CRLF);
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, String line) throws Exception {

		LOG.debug("IN: {}", line);
		
		if (!mailSession.isCommandMode()) {
			try {
				boolean endOfTransfer = mailSession.processDataLine(line);
				if (endOfTransfer) {
					mailManager.save(mailSession);
					mailSession.reset();
					String response = "250 " + DSNStatus.getStatus(DSNStatus.SUCCESS, DSNStatus.CONTENT_OTHER) + " Message received";
					ctx.write(response + StringUtils.CRLF);
					LOG.debug("OUT: {}", response);
				}
			} catch (MailSizeException e) {
				mailSession.reset();
				String response = "552 " + DSNStatus.getStatus(DSNStatus.PERMANENT, DSNStatus.SYSTEM_MSG_TOO_BIG) + " Message is too big";
				ctx.write(response + StringUtils.CRLF);
			}
			return;
		}

		Tuple<String, String> request = Tuple.splitString(line, SmtpCommand.SEPARATOR);
		String command = request.getFirst().toUpperCase();

		String response = mailCommandProcessor.process(mailSession, request);
		ChannelFuture future = ctx.write(response + StringUtils.CRLF);
		LOG.debug("OUT: {}", response);
		
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
