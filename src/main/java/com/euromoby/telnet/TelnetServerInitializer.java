package com.euromoby.telnet;

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
import com.euromoby.processor.CommandProcessor;

@Component
public class TelnetServerInitializer extends ChannelInitializer<SocketChannel> {

	private static final StringDecoder DECODER = new StringDecoder();
	private static final StringEncoder ENCODER = new StringEncoder();

	private CommandProcessor commandProcessor;
	private Config config;

	@Autowired
	public TelnetServerInitializer(Config config, CommandProcessor commandProcessor) {
		this.config = config;
		this.commandProcessor = commandProcessor;
	}

	@Override
	public void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();

		// Add the text line codec combination first,
		pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
		// the encoder and decoder are static as these are sharable
		pipeline.addLast(DECODER);
		pipeline.addLast(ENCODER);

		pipeline.addLast(new IdleStateHandler(0, 0, config.getServerTimeout()));
		pipeline.addLast(new ReadWriteTimeoutHandler());

		// and then business logic.
		pipeline.addLast(new TelnetServerHandler(commandProcessor));
	}
}