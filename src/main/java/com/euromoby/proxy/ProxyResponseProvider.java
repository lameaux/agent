package com.euromoby.proxy;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.http.AsyncHttpClientProvider;
import com.euromoby.http.HttpUtils;
import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.RequestBuilder;

@Component
public class ProxyResponseProvider {

	private static final Logger log = LoggerFactory.getLogger(ProxyResponseProvider.class);
	
	public static final List<String> REQUEST_HEADERS_TO_REMOVE = Arrays.asList(HttpHeaders.Names.COOKIE);
	public static final List<String> RESPONSE_HEADERS_TO_REMOVE = Arrays.asList(HttpHeaders.Names.TRANSFER_ENCODING, HttpHeaders.Names.SERVER,
			HttpHeaders.Names.CONTENT_ENCODING, HttpHeaders.Names.EXPIRES, HttpHeaders.Names.CACHE_CONTROL);
	
	
	private AsyncHttpClientProvider asyncHttpClientProvider;
	
	@Autowired
	public ProxyResponseProvider(AsyncHttpClientProvider asyncHttpClientProvider) {
		this.asyncHttpClientProvider = asyncHttpClientProvider;
	}
	
	public void proxy(final ChannelHandlerContext ctx, FullHttpRequest httpRequest, String sourceUrl) {
		// TODO real proxying (AsyncClient)
		try {
			AsyncHttpClient client = asyncHttpClientProvider.createAsyncHttpClient();
			URI uri = new URI(sourceUrl);
			
			RequestBuilder requestBuilder = asyncHttpClientProvider.createRequestBuilder(uri.getHost(), false);
			requestBuilder.setUrl(sourceUrl);
			// only GET is supported
			requestBuilder.setMethod(HttpMethod.GET.name());
			requestBuilder.setHeaders(prepareHeaders(httpRequest.headers(), uri.getHost()));

			
			AsyncHandler<String> asyncHandler = new AsyncHandler<String>() {
			    //private final Response.ResponseBuilder builder = new Response.ResponseBuilder();
			    private com.ning.http.client.HttpResponseStatus status;

			    @Override
				public STATE onBodyPartReceived(final HttpResponseBodyPart content) throws Exception {
			        //content.write(myOutputStream);
			    	log.debug("onBodyPartReceived");
			    	
			    	
			        return STATE.CONTINUE;
			    }

			    @Override
			    public STATE onStatusReceived(final com.ning.http.client.HttpResponseStatus status) throws Exception {
			    	log.debug("onStatusReceived {}", status.getStatusCode());
			    	this.status = status;
			    	
			    	//builder.accumulate(status); 
			        return STATE.CONTINUE;
			    }

			    @Override
				public STATE onHeadersReceived(final HttpResponseHeaders headers) throws Exception {
			    	log.debug("onHeadersReceived");
			    	
			    	HttpVersion httpVersion = new HttpVersion(status.getProtocolName(), status.getProtocolMajorVersion(), status.getProtocolMinorVersion(), true);
			    	HttpResponseStatus responseStatus = new HttpResponseStatus(status.getStatusCode(), status.getStatusText());
			    	HttpResponse response = new DefaultHttpResponse(httpVersion, responseStatus);
			    	
			    	Map<String, List<String>> headersMap = headers.getHeaders();
			    	HttpHeaders httpHeaders = response.headers();
			    	for (String headerName : headersMap.keySet()) {
			    		httpHeaders.set(headerName, headersMap.get(headerName));
			    	}
			    	ctx.write(response);
			    	
			    	//builder.accumulate(headers);
			        return STATE.CONTINUE;
			    }

			    @Override
				public String onCompleted() throws Exception {
			    	log.debug("onCompleted");
			    	//return builder.build();
			    	return null;
			    }

				@Override
				public void onThrowable(Throwable t) {
			    	log.debug("onThrowable", t);
				}

			};			
			
			
			String response = client.prepareRequest(requestBuilder.build()).execute(asyncHandler).get();
			
			writeErrorResponse(ctx, HttpResponseStatus.GATEWAY_TIMEOUT, HttpResponseStatus.GATEWAY_TIMEOUT.reasonPhrase());
			
		} catch (Exception e) {
			writeErrorResponse(ctx, HttpResponseStatus.GATEWAY_TIMEOUT, HttpResponseStatus.GATEWAY_TIMEOUT.reasonPhrase());
		}
	}

	protected Map<String, Collection<String>> prepareHeaders(HttpHeaders httpHeaders, String host) {
		Map<String, Collection<String>> headers = new HashMap<String, Collection<String>>();
		
		for (String httpHeaderName : httpHeaders.names()) {
			if (HttpHeaders.Names.HOST.equalsIgnoreCase(httpHeaderName)) {
				headers.put(httpHeaderName, Arrays.asList(host));
				continue;
			}
			if (REQUEST_HEADERS_TO_REMOVE.contains(httpHeaderName)) {
				continue;
			}
			headers.put(httpHeaderName, httpHeaders.getAll(httpHeaderName));
		}
		return headers;
	}
	
	protected void writeErrorResponse(ChannelHandlerContext ctx, HttpResponseStatus status, String message) {
		// Build the response object.
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, HttpUtils.fromString(message));
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
		// Write the response.
		ChannelFuture future = ctx.channel().writeAndFlush(response);
		future.addListener(ChannelFutureListener.CLOSE);
	}	
	
}
