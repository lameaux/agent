package com.euromoby.rest.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.ServerCookieEncoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.EndOfDataDecoderException;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.euromoby.http.HttpResponseProvider;
import com.euromoby.rest.RestException;


public abstract class RestHandlerBase implements RestHandler {

	protected HttpRequest request;
	protected HttpPostRequestDecoder decoder;
	protected InetAddress clientInetAddress;

	protected Map<String, String> requestParameters = new HashMap<String, String>();
	protected Map<String, File> requestFiles = new HashMap<String, File>();

	@Override
	public void setHttpRequest(HttpRequest request) {
		this.request = request;
	}

	@Override
	public void setHttpPostRequestDecoder(HttpPostRequestDecoder decoder) {
		this.decoder = decoder;
	}

	public Map<String, String> getRequestParameters() {
		return requestParameters;
	}

	public void setRequestParameters(Map<String, String> requestParameters) {
		this.requestParameters = requestParameters;
	}
	
	public Map<String, File> getRequestFiles() {
		return requestFiles;
	}

	public void setRequestFiles(Map<String, File> requestFiles) {
		this.requestFiles = requestFiles;
	}
	
	
	@Override
	public abstract boolean matchUri(URI uri);	
	
	@Override
	public void process(ChannelHandlerContext ctx) {
		if (request == null) {
			throw new RuntimeException("No HttpRequest");
		}
		
		setClientInetAddress(((InetSocketAddress) ctx.channel().remoteAddress()).getAddress());
		
		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);

		FullHttpResponse response;
		try {
			if (request.getMethod().equals(HttpMethod.POST)) {
				processPostData();
				response = doPost();
			} else if (isChunkedResponse()){
				doGetChunked(ctx);
				return;
			} else {
				response = doGet();
			}
		} catch (RestException e) {
			response = httpResponseProvider.errorResponse(e);
		} catch (Exception e) {
			response = httpResponseProvider.errorResponse(new RestException(HttpResponseStatus.BAD_REQUEST, e));
		} finally {
			if (request.getMethod().equals(HttpMethod.POST)) {
				deleteTempFiles();
			}
		}
		writeResponse(ctx.channel(), response);
	}

	public FullHttpResponse doGet() throws Exception {
		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
		return httpResponseProvider.createHttpResponse(HttpResponseStatus.NOT_IMPLEMENTED);
	}

	public FullHttpResponse doPost() throws Exception {
		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
		return httpResponseProvider.createHttpResponse(HttpResponseStatus.NOT_IMPLEMENTED);
	}

	public boolean isChunkedResponse() {
		return false;
	}
	
	public void doGetChunked(ChannelHandlerContext ctx) throws Exception {
		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
		writeResponse(ctx.channel(), httpResponseProvider.createHttpResponse(HttpResponseStatus.NOT_IMPLEMENTED));
	}
	
	public HttpHeaders getHeaders() {
		return request.headers();
	}

	public Set<Cookie> getCookies() {
		String value = request.headers().get(HttpHeaders.Names.COOKIE);
		if (value == null) {
			return Collections.emptySet();
		} else {
			return CookieDecoder.decode(value);
		}
	}

	public InetAddress getClientInetAddress() {
		return clientInetAddress;
	}

	protected void setClientInetAddress(InetAddress clientInetAddress) {
		this.clientInetAddress = clientInetAddress;
	}	
	
	public Map<String, List<String>> getUriAttributes() {
		QueryStringDecoder decoderQuery = new QueryStringDecoder(request.getUri());
		return decoderQuery.parameters();
	}

	protected void writeResponse(Channel channel, FullHttpResponse response) {
		Set<Cookie> cookies;
		String value = request.headers().get(HttpHeaders.Names.COOKIE);
		if (value == null) {
			cookies = Collections.emptySet();
		} else {
			cookies = CookieDecoder.decode(value);
		}
		if (!cookies.isEmpty()) {
			// Reset the cookies if necessary.
			for (Cookie cookie : cookies) {
				response.headers().add(HttpHeaders.Names.SET_COOKIE, ServerCookieEncoder.encode(cookie));
			}
		}
		// Write the response.
		ChannelFuture future = channel.writeAndFlush(response);

		// Close the connection after the write operation is done if necessary.
		if (!response.headers().contains(HttpHeaders.Names.CONTENT_LENGTH)) {
			future.addListener(ChannelFutureListener.CLOSE);
		}
	}

	/**
	 * Reading POST data
	 */
	protected void processPostData() throws IOException {
		try {
			while (decoder.hasNext()) {
				InterfaceHttpData data = decoder.next();
				if (data != null) {
					try {
						if (data.getHttpDataType() == HttpDataType.Attribute) {
							Attribute attribute = (Attribute) data;
							requestParameters.put(attribute.getName(), attribute.getValue());
						} else if (data.getHttpDataType() == HttpDataType.FileUpload) {
							FileUpload fileUpload = (FileUpload) data;
							if (fileUpload.isCompleted() && fileUpload.length() > 0) {
								File tempFile = File.createTempFile("agent", "upload");
								fileUpload.renameTo(tempFile);
								requestFiles.put(fileUpload.getName(), tempFile);
							}
						}

					} finally {
						data.release();
					}
				}
			}
		} catch (EndOfDataDecoderException e1) {
			// ok
		}
	}

	protected void deleteTempFiles() {
		for (File tempFile : requestFiles.values()) {
			tempFile.delete();
		}
	}

	
}
