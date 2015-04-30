package com.euromoby.network;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadWriteTimeoutHandler extends ChannelDuplexHandler {
	private static final Logger log = LoggerFactory.getLogger(ReadWriteTimeoutHandler.class);
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent e = (IdleStateEvent) evt;
			if (e.state() == IdleState.ALL_IDLE) {
				ctx.close();
				log.debug("Closing idle connection from {}", ctx.channel().remoteAddress());
			}
		}
	}
}
