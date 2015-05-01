package com.euromoby.mail;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.Config;
import com.euromoby.network.ReadWriteTimeoutHandler;

@Component
public class MailServerInitializer extends ChannelInitializer<SocketChannel> {

	private static final StringDecoder DECODER = new StringDecoder();
	private static final StringEncoder ENCODER = new StringEncoder();

	private Config config;
	private SmtpCommandProcessor mailCommandProcessor;
	private MailManager mailManager;

	@Autowired
	public MailServerInitializer(Config config, SmtpCommandProcessor mailCommandProcessor, MailManager mailManager) {
		this.config = config;
		this.mailCommandProcessor = mailCommandProcessor;
		this.mailManager = mailManager;
	}

	@Override
	public void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();

		pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
		pipeline.addLast(DECODER);
		pipeline.addLast(ENCODER);
		pipeline.addLast(new IdleStateHandler(0, 0, config.getServerTimeout()));
		pipeline.addLast(new ReadWriteTimeoutHandler());

		pipeline.addLast(new MailServerHandler(config, mailCommandProcessor, mailManager));
	}
}