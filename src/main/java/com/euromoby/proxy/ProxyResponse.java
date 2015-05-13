package com.euromoby.proxy;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;

import java.nio.channels.ClosedChannelException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.euromoby.http.AsyncHttpClientProvider;
import com.euromoby.http.HttpUtils;
import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;

public class ProxyResponse {

	private static final Logger log = LoggerFactory.getLogger(ProxyResponse.class);
	
	public static final List<String> REQUEST_HEADERS_TO_REMOVE = Arrays.asList(
			HttpHeaders.Names.COOKIE.toLowerCase(), 
			HttpHeaders.Names.HOST.toLowerCase()
			);
	public static final List<String> RESPONSE_HEADERS_TO_REMOVE = Arrays.asList(
			HttpHeaders.Names.TRANSFER_ENCODING.toLowerCase(),
			HttpHeaders.Names.SET_COOKIE.toLowerCase(), 			
			HttpHeaders.Names.SERVER.toLowerCase(),
			HttpHeaders.Names.VIA.toLowerCase(),
			HttpHeaders.Names.CONTENT_ENCODING.toLowerCase() 
			);
	
	
	private AsyncHttpClientProvider asyncHttpClientProvider;
	
	public ProxyResponse(AsyncHttpClientProvider asyncHttpClientProvider) {
		this.asyncHttpClientProvider = asyncHttpClientProvider;
	}
	
	public void proxy(ChannelHandlerContext ctx, FullHttpRequest httpRequest, String sourceUrl) {
		try {
			BoundRequestBuilder boundRequestBuilder = asyncHttpClientProvider.prepareGet(sourceUrl, false);
			boundRequestBuilder.setHeaders(prepareHeaders(httpRequest.headers()));
			boundRequestBuilder.execute(new ProxyAsyncHandler(ctx, httpRequest)).get();
		} catch (Exception e) {
			writeStatusResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, HttpResponseStatus.INTERNAL_SERVER_ERROR.reasonPhrase());
		}
	}

	protected Map<String, Collection<String>> prepareHeaders(HttpHeaders httpHeaders) {
		Map<String, Collection<String>> headers = new HashMap<String, Collection<String>>();
		
		for (String httpHeaderName : httpHeaders.names()) {
			if (REQUEST_HEADERS_TO_REMOVE.contains(httpHeaderName.toLowerCase())) {
				continue;
			}
			headers.put(httpHeaderName, httpHeaders.getAll(httpHeaderName));
		}
		return headers;
	}

	protected boolean supportChunks(FullHttpRequest httpRequest) {
		return httpRequest.getProtocolVersion().equals(HttpVersion.HTTP_1_1);
	}	
	
	protected void writeStatusResponse(ChannelHandlerContext ctx, HttpResponseStatus status, String message) {
		// Build the response object.
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, HttpUtils.fromString(message));
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
		// Write the response.
		ChannelFuture future = ctx.channel().writeAndFlush(response);
		future.addListener(ChannelFutureListener.CLOSE);
	}	

	class ProxyAsyncHandler implements AsyncHandler<String> {
		
		private ChannelHandlerContext ctx;
		private FullHttpRequest httpRequest;
        private int responseCode = HttpResponseStatus.OK.code();
        
		public ProxyAsyncHandler(ChannelHandlerContext ctx, FullHttpRequest httpRequest) {
			this.ctx = ctx;
			this.httpRequest = httpRequest;
		}
		
	    @Override
	    public STATE onStatusReceived(final com.ning.http.client.HttpResponseStatus httpResponseStatus) throws Exception {
	    	log.trace("onStatusReceived {}", httpResponseStatus.getStatusCode());

            if (httpResponseStatus.getStatusCode() >= 200 && httpResponseStatus.getStatusCode() < 300) {
                responseCode = httpResponseStatus.getStatusCode();
                return STATE.CONTINUE;
            }
            if (httpResponseStatus.getStatusCode() == HttpResponseStatus.NOT_MODIFIED.code()) {
            	writeStatusResponse(ctx, HttpResponseStatus.NOT_MODIFIED, HttpResponseStatus.NOT_MODIFIED.reasonPhrase());
                return STATE.ABORT;
            }
            HttpResponseStatus errorStatus = HttpResponseStatus.valueOf(httpResponseStatus.getStatusCode());
            writeStatusResponse(ctx, errorStatus, errorStatus.reasonPhrase());
            return STATE.ABORT;			    	
	    }

	    @Override
		public STATE onHeadersReceived(final HttpResponseHeaders headers) throws Exception {
	    	log.trace("onHeadersReceived");
	    	
	    	HttpResponse response = new DefaultHttpResponse(httpRequest.getProtocolVersion(), HttpResponseStatus.valueOf(responseCode));
	    	
	    	Map<String, List<String>> headersMap = headers.getHeaders();
	    	HttpHeaders httpHeaders = response.headers();
	    	for (String headerName : headersMap.keySet()) {
	    		if (RESPONSE_HEADERS_TO_REMOVE.contains(headerName.toLowerCase())) {
	    			continue;
	    		}
	    		if (headerName.toLowerCase().startsWith("x-")) {
	    			continue;
	    		}
	    		if (headerName.toLowerCase().startsWith("proxy")) {
	    			continue;
	    		}	    		
	    		httpHeaders.set(headerName, headersMap.get(headerName));
	    	}
	    	
			if (supportChunks(httpRequest)) {
				httpHeaders.set(HttpHeaders.Names.TRANSFER_ENCODING, HttpHeaders.Values.CHUNKED);
			}			    	
	    	
	    	ctx.write(response);
	    	
	        return STATE.CONTINUE;
	    }

	    @Override
		public STATE onBodyPartReceived(final HttpResponseBodyPart bodyPart) throws Exception {
	    	log.trace("onBodyPartReceived");
	    	
	    	if (supportChunks(httpRequest)) {
	    		ctx.write(new DefaultHttpContent(Unpooled.wrappedBuffer(bodyPart.getBodyByteBuffer())));
	    		
	    	} else {
	    		ctx.write(Unpooled.wrappedBuffer(bodyPart.getBodyByteBuffer()));
	    	}
	    	
	    	return STATE.CONTINUE;
	    }			    
	    
	    @Override
		public String onCompleted() throws Exception {
	    	log.trace("onCompleted");
	    	
    		ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
    		if (!HttpHeaders.isKeepAlive(httpRequest)) {
    			lastContentFuture.addListener(ChannelFutureListener.CLOSE);
    		}			    	
	    	
	    	return "OK";
	    }

		@Override
		public void onThrowable(Throwable t) {
			if (!(t instanceof ClosedChannelException)) {
				log.trace("onThrowable", t);
				writeStatusResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, HttpResponseStatus.INTERNAL_SERVER_ERROR.reasonPhrase());
			}
		}

	};			

	
	
}
