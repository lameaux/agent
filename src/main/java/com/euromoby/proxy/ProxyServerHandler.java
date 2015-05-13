package com.euromoby.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;

import java.net.URI;

public class ProxyServerHandler extends SimpleChannelInboundHandler<HttpObject> {

	private static final int PORT_UNDEFINED = -1;
	private static final int PORT_DEFAULT = 80;
	
	private volatile Channel outboundChannel;
	
	public ProxyServerHandler() {
	}

	@Override
	public void channelRead0(final ChannelHandlerContext ctx, HttpObject httpObject) throws Exception {
		
		if (httpObject instanceof HttpRequest) {
			HttpRequest httpRequest = (HttpRequest) httpObject;
			URI uri = new URI(httpRequest.getUri());
			String host = uri.getHost();
			int port = uri.getPort();
			if (port == PORT_UNDEFINED) {
				port = PORT_DEFAULT;
			}
			
			final Channel inboundChannel = ctx.channel();
			
	        // Start the connection attempt.
	        Bootstrap b = new Bootstrap();
	        b.group(inboundChannel.eventLoop())
	         .channel(ctx.channel().getClass())
	         .handler(new ProxyClientHandler(inboundChannel))
	         .option(ChannelOption.AUTO_READ, false);
	        ChannelFuture f = b.connect(host, port);
	        outboundChannel = f.channel();
	        f.addListener(new ChannelFutureListener() {
	            @Override
	            public void operationComplete(ChannelFuture future) {
	                if (future.isSuccess()) {
	                    // connection complete start to read first data
	                    inboundChannel.read();
	                } else {
	                    // Close the connection if the connection attempt has failed.
	                    inboundChannel.close();
	                }
	            }
	        });			
			
		}
		
		if (outboundChannel.isActive()) {
            outboundChannel.writeAndFlush(httpObject).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess()) {
                        // was able to flush out data, start to read the next chunk
                        ctx.channel().read();
                    } else {
                        future.channel().close();
                    }
                }
            });
        }		
		
	}

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
    	closeOnFlush(outboundChannel);
    }	

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		closeOnFlush(outboundChannel);
	}

    private void closeOnFlush(Channel ch) {
        if (ch != null && ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }	
	
}