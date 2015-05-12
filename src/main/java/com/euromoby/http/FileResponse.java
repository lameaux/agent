package com.euromoby.http;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import com.euromoby.file.MimeHelper;
import com.euromoby.model.Tuple;
import com.euromoby.rest.ChunkedInputAdapter;
import com.euromoby.rest.RestException;
import com.euromoby.utils.StringUtils;

public class FileResponse {

	private static final Pattern RANGE_HEADER = Pattern.compile("bytes=(\\d+)?\\-(\\d+)?");
	public static final int HTTP_CHUNK_SIZE = 8192;
	public static final String MAX_AGE_VALUE = "max-age=";
	public static final String CONTENT_DISPOSITION = "Content-Disposition";
	public static final String CONTENT_DISPOSITION_ATTACHMENT = "attachment";
	public static final String CONTENT_DISPOSITION_INLINE = "inline";

	private HttpRequest request;
	private MimeHelper mimeHelper;

	private HttpResponseStatus status = HttpResponseStatus.OK;
	private Map<String, String> headers = new HashMap<String, String>();

	public FileResponse(HttpRequest request, MimeHelper mimeHelper) {
		this.request = request;
		this.mimeHelper = mimeHelper;
	}

	public void setHeader(String name, String value) {
		headers.put(name, value);
	}

	protected String getHeader(String name) {
		return headers.get(name);
	}

	public void send(ChannelHandlerContext ctx, File file) throws RestException {

		if (!file.exists()) {
			throw new RestException(HttpResponseStatus.NOT_FOUND, "Not found");
		}

		setHeaderTransferEncoding();
		setHeaderContentEncoding(file, isSSL(ctx));
		setHeaderKeepAlive();
		setHeader(HttpHeaders.Names.ACCEPT_RANGES, HttpHeaders.Values.BYTES);
		setHeaderContentType(file);
		setHeaderContentDisposition(file);
		setDateAndCacheHeaders(file);

		long fileLength = file.length();

		Tuple<Long, Long> range;
		try {
			range = parseRange(fileLength);
		} catch (IllegalArgumentException e) {
			throw new RestException(HttpResponseStatus.REQUESTED_RANGE_NOT_SATISFIABLE, e);
		}

		if (range == null) {
			setHeaderContentLength(fileLength);
		} else {
			status = HttpResponseStatus.PARTIAL_CONTENT;
			setHeaderContentLength(range.getSecond() - range.getFirst() + 1);
			setHeaderContentRange(range, fileLength);
		}

		HttpResponse response = new DefaultHttpResponse(request.getProtocolVersion(), status);
		setupResponseHeaders(response);
		ctx.write(response);

		// calculate content range
		long contentOffset = 0;
		long contentLength = fileLength;
		if (range != null) {
			contentOffset = range.getFirst();
			contentLength = range.getSecond() - range.getFirst() + 1;
		}

		try {
			sendFileContent(ctx, file, contentOffset, contentLength);
		} catch (IOException e) {
			throw new RestException(HttpResponseStatus.INTERNAL_SERVER_ERROR, "Error");
		}

	}

	protected void sendFileContent(ChannelHandlerContext ctx, File file, long offset, long length) throws IOException {

		final RandomAccessFile raf = new RandomAccessFile(file, "r");
		sendFileBody(ctx, raf, offset, length);

		ChannelFuture lastContentFuture = ctx.writeAndFlush(DefaultLastHttpContent.EMPTY_LAST_CONTENT);
		lastContentFuture.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture channelFuture) throws Exception {
				IOUtils.closeQuietly(raf);
			}
		});
		if (!HttpHeaders.isKeepAlive(request)) {
			lastContentFuture.addListener(ChannelFutureListener.CLOSE);
		}

	}

	protected void sendFileBody(ChannelHandlerContext ctx, RandomAccessFile raf, long offset, long length) throws IOException {
		if (isSSL(ctx)) {
			ctx.write(new ChunkedFile(raf, offset, length, HTTP_CHUNK_SIZE));
		} else if (supportChunks()) {
			ctx.write(new ChunkedInputAdapter(new ChunkedFile(raf, offset, length, HTTP_CHUNK_SIZE)));
		} else {
			ctx.write(new DefaultFileRegion(raf.getChannel(), offset, length));
		}		
	}
	
	protected boolean supportChunks() {
		return request.getProtocolVersion().equals(HttpVersion.HTTP_1_1);
	}

	protected void setHeaderContentLength(long contentLength) {
		setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(contentLength));
	}

	protected void setHeaderContentRange(Tuple<Long, Long> range, long contentLength) {
		setHeader(HttpHeaders.Names.CONTENT_RANGE, "bytes " + range.getFirst() + "-" + range.getSecond() + "/" + contentLength);
	}

	protected void setHeaderTransferEncoding() {
		if (supportChunks()) {
			setHeader(HttpHeaders.Names.TRANSFER_ENCODING, HttpHeaders.Values.CHUNKED);
		}
	}

	protected void setHeaderKeepAlive() {
		if (HttpHeaders.isKeepAlive(request)) {
			setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
		}
	}

	protected void setHeaderContentEncoding(File file, boolean ssl) {
		if (!mimeHelper.isCompressible(file) || ssl) {
			setHeader(HttpHeaders.Names.CONTENT_ENCODING, HttpHeaders.Values.IDENTITY);
		}
	}

	protected void setHeaderContentType(File file) {
		String mimeType = mimeHelper.getContentType(file);
		setHeader(HttpHeaders.Names.CONTENT_TYPE, mimeType + "; charset=UTF-8");
	}

	protected void setHeaderContentDisposition(File file) {
		boolean download = mimeHelper.isBinary(file);
		String name = file.getName();
		setHeader(CONTENT_DISPOSITION,
				(download ? CONTENT_DISPOSITION_ATTACHMENT : CONTENT_DISPOSITION_INLINE) + ";filename=\"" + name.replaceAll("[^A-Za-z0-9\\-_\\.]", "_") + "\"");
	}

	protected Tuple<Long, Long> parseRange(long availableLength) {
		String header = request.headers().get(HttpHeaders.Names.RANGE);
		if (StringUtils.nullOrEmpty(header)) {
			return null;
		}
		Matcher m = RANGE_HEADER.matcher(header);
		if (!m.matches()) {
			throw new IllegalArgumentException("Unsupported range: " + header);
		}
		Tuple<Long, Long> result = Tuple.empty();
		
        if (!StringUtils.nullOrEmpty(m.group(1))) {
            result.setFirst(Long.parseLong(m.group(1)));
        } else {
            result.setFirst(availableLength - Long.parseLong(m.group(2)));
            result.setSecond(availableLength - 1);
            return result;
        }
        
		if (!StringUtils.nullOrEmpty(m.group(2))) {
			result.setSecond(Long.parseLong(m.group(2)));
		} else {
			result.setSecond(availableLength - 1);
		}
		if (result.getSecond() < result.getFirst()) {
			return null;
		}
		if (result.getSecond() >= availableLength) {
			throw new IllegalArgumentException("Unsupported range: " + header);
		}

		return result;
	}

	protected void setDateAndCacheHeaders(File fileToCache) {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(HttpUtils.HTTP_DATE_FORMAT, Locale.US);
		dateFormatter.setTimeZone(TimeZone.getTimeZone(HttpUtils.HTTP_DATE_GMT_TIMEZONE));

		// Date header
		Calendar time = new GregorianCalendar();
		setHeader(HttpHeaders.Names.DATE, dateFormatter.format(time.getTime()));

		// Add cache headers
		time.add(Calendar.SECOND, HttpUtils.HTTP_CACHE_SECONDS);
		setHeader(HttpHeaders.Names.EXPIRES, dateFormatter.format(time.getTime()));
		setHeader(HttpHeaders.Names.CACHE_CONTROL, MAX_AGE_VALUE + HttpUtils.HTTP_CACHE_SECONDS);
		setHeader(HttpHeaders.Names.LAST_MODIFIED, dateFormatter.format(new Date(fileToCache.lastModified())));
	}

	protected void setupResponseHeaders(HttpResponse response) {
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			response.headers().set(entry.getKey(), entry.getValue());
		}
	}

	protected boolean isSSL(ChannelHandlerContext ctx) {
		return ctx.channel().pipeline().get(SslHandler.class) != null;
	}

}
