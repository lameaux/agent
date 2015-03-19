package rest.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.ServerCookieEncoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.EndOfDataDecoderException;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import io.netty.util.CharsetUtil;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RestHandlerBase implements RestHandler {

	protected HttpRequest request;
	protected HttpPostRequestDecoder decoder;

	protected Map<String, String> requestParameters = new HashMap<String, String>();
	protected Map<String, File> requestFiles = new HashMap<String, File>();

	public void setHttpRequest(HttpRequest request) {
		this.request = request;
	}

	public void setHttpPostRequestDecoder(HttpPostRequestDecoder decoder) {
		this.decoder = decoder;
	}

	public Map<String, String> getRequestParameters() {
		return requestParameters;
	}

	public Map<String, File> getRequestFiles() {
		return requestFiles;
	}

	public void process(ChannelHandlerContext ctx) {
		if (request == null) {
			throw new RuntimeException("No HttpRequest");
		}
		FullHttpResponse response;
		try {
			if (request.getMethod().equals(HttpMethod.POST)) {
				processPostData();
				response = doPost();
			} else {
				response = doGet();
			}
		} catch (IOException e) {
			response = errorResponse(e);
		} finally {
			if (request.getMethod().equals(HttpMethod.POST)) {
				deleteTempFiles();
			}
		}
		writeResponse(ctx.channel(), response);
	}

	public FullHttpResponse doGet() {
		return getFullHttpResponse(Unpooled.copiedBuffer("GET", CharsetUtil.UTF_8));
	}

	public FullHttpResponse doPost() throws IOException {
		return getFullHttpResponse(Unpooled.copiedBuffer("POST", CharsetUtil.UTF_8));
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

	public Map<String, List<String>> getUriAttributes() {
		QueryStringDecoder decoderQuery = new QueryStringDecoder(request.getUri());
		return decoderQuery.parameters();
	}

	protected FullHttpResponse getFullHttpResponse() {
		return getFullHttpResponse(Unpooled.buffer(0));
	}

	protected FullHttpResponse getFullHttpResponse(ByteBuf buf) {
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");

		// Decide whether to close the connection or not.
		boolean close = request.headers().contains(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE, true)
				|| request.getProtocolVersion().equals(HttpVersion.HTTP_1_0)
				&& !request.headers().contains(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE, true);

		if (!close) {
			// There's no need to add 'Content-Length' header
			// if this is the last response.
			response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, buf.readableBytes());
		}
		return response;
	}

	private void writeResponse(Channel channel, FullHttpResponse response) {

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

	protected FullHttpResponse errorResponse(Exception e) {
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(e.getMessage(),
				CharsetUtil.UTF_8));
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
		return response;
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
							if (fileUpload.isCompleted()) {
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

	private void deleteTempFiles() {
		for (File tempFile : requestFiles.values()) {
			tempFile.delete();
		}
	}

}
