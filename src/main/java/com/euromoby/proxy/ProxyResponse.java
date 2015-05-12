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
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;

import java.net.URI;
import java.nio.channels.ClosedChannelException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.euromoby.http.AsyncHttpClientProvider;
import com.euromoby.http.HttpUtils;
import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.RequestBuilder;

public class ProxyResponse {

	private static final Logger log = LoggerFactory.getLogger(ProxyResponse.class);
	
	public static final List<String> REQUEST_HEADERS_TO_REMOVE = Arrays.asList(HttpHeaders.Names.COOKIE.toLowerCase());
	public static final List<String> RESPONSE_HEADERS_TO_REMOVE = Arrays.asList(HttpHeaders.Names.TRANSFER_ENCODING.toLowerCase(), HttpHeaders.Names.SERVER.toLowerCase(),
			HttpHeaders.Names.CONTENT_ENCODING.toLowerCase(), HttpHeaders.Names.EXPIRES.toLowerCase(), HttpHeaders.Names.CACHE_CONTROL.toLowerCase());
	
	
	private AsyncHttpClientProvider asyncHttpClientProvider;
	
	public ProxyResponse(AsyncHttpClientProvider asyncHttpClientProvider) {
		this.asyncHttpClientProvider = asyncHttpClientProvider;
	}
	
	public void proxy(final ChannelHandlerContext ctx, final FullHttpRequest httpRequest, String sourceUrl) {
		AsyncHttpClient client = asyncHttpClientProvider.createAsyncHttpClient();
		try {
			URI uri = new URI(sourceUrl);
			
			RequestBuilder requestBuilder = asyncHttpClientProvider.createRequestBuilder(uri.getHost(), false);
			requestBuilder.setUrl(sourceUrl);
			// only GET is supported
			requestBuilder.setMethod(HttpMethod.GET.name());
			requestBuilder.setHeaders(prepareHeaders(httpRequest.headers(), uri.getHost()));

			
			AsyncHandler<String> asyncHandler = new AsyncHandler<String>() {
				
                private int responseCode = HttpResponseStatus.OK.code();
                private boolean chunkedResponse = true;

			    @Override
			    public STATE onStatusReceived(final com.ning.http.client.HttpResponseStatus httpResponseStatus) throws Exception {
			    	log.debug("onStatusReceived {}", httpResponseStatus.getStatusCode());

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
			    	log.debug("onHeadersReceived");
			    	
			    	HttpResponse response = new DefaultHttpResponse(httpRequest.getProtocolVersion(), HttpResponseStatus.valueOf(responseCode));
			    	
			    	Map<String, List<String>> headersMap = headers.getHeaders();
			    	HttpHeaders httpHeaders = response.headers();
			    	for (String headerName : headersMap.keySet()) {
			    		if (RESPONSE_HEADERS_TO_REMOVE.contains(headerName.toLowerCase())) {
			    			continue;
			    		}
			    		if (HttpHeaders.Names.CONTENT_LENGTH.equals(headerName)) {
			    			chunkedResponse = false;
			    		}
			    		httpHeaders.set(headerName, headersMap.get(headerName));
			    	}
			    	
			    	// TODO check Cache/Expired/Modified headers
			    	
			    	ctx.write(response);
			    	
			        return STATE.CONTINUE;
			    }

			    @Override
				public STATE onBodyPartReceived(final HttpResponseBodyPart bodyPart) throws Exception {
			    	log.debug("onBodyPartReceived");
			    	
			    	if (bodyPart.isLast()) {
			    		 
			    		if (chunkedResponse) {
			    			ctx.channel().write(new DefaultHttpContent(Unpooled.wrappedBuffer(bodyPart.getBodyByteBuffer())));
			    		} else {
                            ctx.channel().write(Unpooled.wrappedBuffer(bodyPart.getBodyByteBuffer()));
			    		}

			    		ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
			    		if (!HttpHeaders.isKeepAlive(httpRequest)) {
			    			lastContentFuture.addListener(ChannelFutureListener.CLOSE);
			    		}			    		
			    		
			    	} else {

                        if (chunkedResponse) {
                            ChannelFuture writeFuture = ctx.channel().writeAndFlush(new DefaultHttpContent(Unpooled.wrappedBuffer(bodyPart.getBodyByteBuffer())));
                            while (!ctx.channel().isWritable() && ctx.channel().isOpen()) {
                                writeFuture.await(5, TimeUnit.SECONDS);
                            }
                        } else {
                            ChannelFuture writeFuture = ctx.channel().writeAndFlush(Unpooled.wrappedBuffer(bodyPart.getBodyByteBuffer()));
                            while (!ctx.channel().isWritable() && ctx.channel().isOpen()) {
                                writeFuture.await(5, TimeUnit.SECONDS);
                            }
                        }			    		
			    		
			    	}
			    	
			        return STATE.CONTINUE;
			    }			    
			    
			    @Override
				public String onCompleted() throws Exception {
			    	log.debug("onCompleted");
			    	return "OK";
			    }

				@Override
				public void onThrowable(Throwable t) {
					if (!(t instanceof ClosedChannelException)) {
						log.debug("onThrowable", t);
						writeStatusResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, HttpResponseStatus.INTERNAL_SERVER_ERROR.reasonPhrase());
					}
				}

			};			
			
			client.prepareRequest(requestBuilder.build()).execute(asyncHandler);
			
		} catch (Exception e) {
			writeStatusResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, HttpResponseStatus.INTERNAL_SERVER_ERROR.reasonPhrase());
		} finally {
			IOUtils.closeQuietly(client);
		}
	}

	protected Map<String, Collection<String>> prepareHeaders(HttpHeaders httpHeaders, String host) {
		Map<String, Collection<String>> headers = new HashMap<String, Collection<String>>();
		
		for (String httpHeaderName : httpHeaders.names()) {
			if (HttpHeaders.Names.HOST.equalsIgnoreCase(httpHeaderName)) {
				headers.put(httpHeaderName, Arrays.asList(host));
				continue;
			}
			if (REQUEST_HEADERS_TO_REMOVE.contains(httpHeaderName.toLowerCase())) {
				continue;
			}
			headers.put(httpHeaderName, httpHeaders.getAll(httpHeaderName));
		}
		return headers;
	}
	
	protected void writeStatusResponse(ChannelHandlerContext ctx, HttpResponseStatus status, String message) {
		// Build the response object.
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, HttpUtils.fromString(message));
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
		// Write the response.
		ChannelFuture future = ctx.channel().writeAndFlush(response);
		future.addListener(ChannelFutureListener.CLOSE);
	}	
	
}
