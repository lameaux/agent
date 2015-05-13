package com.euromoby.rest.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.ClientCookieDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.EndOfDataDecoderException;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.euromoby.http.HttpResponseProvider;
import com.euromoby.model.Tuple;
import com.euromoby.rest.RestException;


public abstract class RestHandlerBase implements RestHandler {

	@Override
	public abstract boolean matchUri(URI uri);	
	
	@Override
	public void process(ChannelHandlerContext ctx, HttpRequest request, HttpPostRequestDecoder decoder) {
		if (request == null) {
			throw new RuntimeException("No HttpRequest");
		}
		
		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);

		FullHttpResponse response;
		try {
			Map<String, List<String>> queryParameters = getUriAttributes(request);
			if (request.getMethod().equals(HttpMethod.POST)) {
				Tuple<Map<String, List<String>>, Map<String, File>> postData = processPostData(decoder);
				try {
					response = doPost(ctx, request, queryParameters, postData.getFirst(), postData.getSecond());
				} finally {
					deleteTempFiles(postData.getSecond());
				}
			} else if (isChunkedResponse()){
				doGetChunked(ctx, request, queryParameters);
				return;
			} else {
				response = doGet(ctx, request, queryParameters);
			}
		} catch (RestException e) {
			response = httpResponseProvider.errorResponse(e);
		} catch (Exception e) {
			response = httpResponseProvider.errorResponse(new RestException(HttpResponseStatus.BAD_REQUEST, e));
		}
		
		httpResponseProvider.writeResponse(ctx, response);
	}

	public FullHttpResponse doGet(ChannelHandlerContext ctx, HttpRequest request, Map<String, List<String>> queryParameters) throws Exception {
		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
		return httpResponseProvider.createHttpResponse(HttpResponseStatus.NOT_IMPLEMENTED);
	}

	public FullHttpResponse doPost(ChannelHandlerContext ctx, HttpRequest request, Map<String, List<String>> queryParameters, Map<String, List<String>> postParameters, Map<String, File> uploadFiles) throws Exception {
		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
		return httpResponseProvider.createHttpResponse(HttpResponseStatus.NOT_IMPLEMENTED);
	}

	public boolean isChunkedResponse() {
		return false;
	}
	
	public void doGetChunked(ChannelHandlerContext ctx, HttpRequest request, Map<String, List<String>> queryParameters) throws Exception {
		HttpResponseProvider httpResponseProvider = new HttpResponseProvider(request);
		FullHttpResponse response = httpResponseProvider.createHttpResponse(HttpResponseStatus.NOT_IMPLEMENTED);
		httpResponseProvider.writeResponse(ctx, response);
	}
	
	public Cookie getCookie(HttpRequest request) {
		String value = request.headers().get(HttpHeaders.Names.COOKIE);
		if (value != null) {
			return ClientCookieDecoder.LAX.decode(value);
		}
		return null;
	}

	public URI getUri(HttpRequest request) {
		if (request == null) {
			return null;
		}
		try {
			return new URI(request.getUri());
		} catch (URISyntaxException e) {
			return null;
		}		
	}	
	
	protected Map<String, List<String>> getUriAttributes(HttpRequest request) {
		QueryStringDecoder decoderQuery = new QueryStringDecoder(request.getUri());
		return decoderQuery.parameters();
	}

	/**
	 * Reading POST data
	 */
	protected Tuple<Map<String, List<String>>, Map<String, File>> processPostData(HttpPostRequestDecoder decoder) throws IOException {
		Map<String, List<String>> requestParameters = new HashMap<String, List<String>>();
		Map<String, File> requestFiles = new HashMap<String, File>();		

		try {
			while (decoder.hasNext()) {
				InterfaceHttpData data = decoder.next();
				if (data != null) {
					try {
						if (data.getHttpDataType() == HttpDataType.Attribute) {
							Attribute attribute = (Attribute) data;
							List<String> values = requestParameters.get(attribute.getName());
							if (values == null) {
								values = new ArrayList<String>();
								requestParameters.put(attribute.getName(), values);
							}
							values.add(attribute.getValue());
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
		
		return Tuple.of(requestParameters, requestFiles);
		
	}

	protected void deleteTempFiles(Map<String, File> files) {
		for (File tempFile : files.values()) {
			tempFile.delete();
		}
	}

	
}
