package rest;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.DiskAttribute;
import io.netty.handler.codec.http.multipart.DiskFileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import io.netty.util.CharsetUtil;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rest.handler.RestHandler;
import rest.handler.cli.CliHandler;
import rest.handler.job.JobAddHandler;
import rest.handler.job.JobListHandler;
import rest.handler.ping.PingHandler;
import rest.handler.upload.UploadHandler;
import rest.handler.welcome.WelcomeHandler;
import agent.Agent;
import agent.Configuration;

public class RestServerHandler extends SimpleChannelInboundHandler<HttpObject> {

	private Configuration config;
	
	private RestHandler handler;
	private HttpRequest request;

	// store on disk if > 16k
	private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);

	static {
		DiskFileUpload.deleteOnExitTemporaryFile = true;
		DiskFileUpload.baseDirectory = null; // system temp directory
		DiskAttribute.deleteOnExitTemporaryFile = true;
		DiskAttribute.baseDirectory = null; // system temp directory
	}	
	
	private HttpPostRequestDecoder decoder;

	private static final Logger LOG = LoggerFactory.getLogger(RestServerHandler.class); 		

	public RestServerHandler() {
		this.config = Agent.get().getConfig();
	}

	private RestHandler getRestHandler() {
		String uriString = request.getUri();
		URI uri;
		try {
			uri = new URI(uriString);
		} catch (URISyntaxException e) {
			return null;
		}
		
		// TODO create mapping
		
		if (uri.getPath().equals(WelcomeHandler.URL)) {
			return new WelcomeHandler();
		}
		if (uri.getPath().equals(CliHandler.URL)) {
			return new CliHandler();
		}
		if (uri.getPath().equals(UploadHandler.URL)) {
			return new UploadHandler(config.getAgentFilesPath());
		}
		if (uri.getPath().equals(PingHandler.URL)) {
			return new PingHandler();
		}
		if (uri.getPath().equals(JobListHandler.URL)) {
			return new JobListHandler();
		}
		if (uri.getPath().equals(JobAddHandler.URL)) {
			return new JobAddHandler();
		}
		
		return null;
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if (decoder != null) {
			decoder.cleanFiles();
		}
	}

	private void processRequest(ChannelHandlerContext ctx) {
		if (handler != null) {
			handler.process(ctx);
		}
	}

	private void processError(ChannelHandlerContext ctx, RestException e) {
		ByteBuf outputBuf = Unpooled.copiedBuffer(e.getMessage(), CharsetUtil.UTF_8);
		writeErrorResponse(ctx.channel(), e.getStatus(), outputBuf);
		ctx.channel().close();
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
		if (msg instanceof HttpRequest) {
			request = (HttpRequest) msg;

			handler = getRestHandler();
			if (handler == null) {
				processError(ctx, new RestException(HttpResponseStatus.NOT_FOUND, "Not found"));
				return;
			}

			// save request
			handler.setHttpRequest(request);

			// if GET Method: should not try to create a HttpPostRequestDecoder
			if (request.getMethod().equals(HttpMethod.GET)) {
				return;
			}
			try {
				decoder = new HttpPostRequestDecoder(factory, request);
				// save decoder
				handler.setHttpPostRequestDecoder(decoder);
			} catch (ErrorDataDecoderException e) {
				processError(ctx, new RestException(e));
				return;
			}
		}

		// check if the decoder was constructed before
		// if not it handles the form get
		if (decoder != null) {
			if (msg instanceof HttpContent) {
				// New chunk is received
				HttpContent chunk = (HttpContent) msg;
				try {
					decoder.offer(chunk);
				} catch (ErrorDataDecoderException e) {
					processError(ctx, new RestException(e));
					return;
				}

				// example of reading only if at the end
				if (chunk instanceof LastHttpContent) {
					processRequest(ctx);
					reset();
				}
			}
		} else {
			processRequest(ctx);
		}
	}

	private void writeErrorResponse(Channel channel, HttpResponseStatus status, ByteBuf buf) {
		// Build the response object.
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, buf);
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");

		// Write the response.
		ChannelFuture future = channel.writeAndFlush(response);
		future.addListener(ChannelFutureListener.CLOSE);
	}

	private void reset() {
		request = null;
		handler = null;
		decoder.destroy();
		decoder = null;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		LOG.debug("Exception", cause);
		ctx.channel().close();
	}

}