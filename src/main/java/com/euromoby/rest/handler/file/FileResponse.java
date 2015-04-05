package com.euromoby.rest.handler.file;

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
import java.io.FileNotFoundException;
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
import com.euromoby.http.HttpUtils;
import com.euromoby.model.Tuple;
import com.euromoby.rest.ChunkedInputAdapter;
import com.euromoby.rest.RestException;
import com.euromoby.utils.StringUtils;

public class FileResponse {

	private static final Pattern RANGE_HEADER = Pattern.compile("bytes=(\\d+)\\-(\\d+)?");
	private static final int HTTP_CHUNK_SIZE = 8192;

	private HttpRequest request;
	private MimeHelper mimeHelper;

	private HttpVersion version = HttpVersion.HTTP_1_1;
	private HttpResponseStatus status = HttpResponseStatus.OK;
	private Map<String, String> headers = new HashMap<String, String>();
	private boolean supportChunks = true;
	private boolean compressible = false;

	public FileResponse(HttpRequest request, MimeHelper mimeHelper) {
		this.request = request;
		this.mimeHelper = mimeHelper;
	}

	public void setHeader(String name, String value) {
		headers.put(name, value);
	}

	public void send(ChannelHandlerContext ctx, File file) throws RestException {
		version = request.getProtocolVersion();
		supportChunks = request.getProtocolVersion() == HttpVersion.HTTP_1_1;
		compressible = mimeHelper.isCompressible(file);

		if (!compressible || isSSL(ctx)) {
			setHeader(HttpHeaders.Names.CONTENT_ENCODING, HttpHeaders.Values.IDENTITY);
		}
		if (supportChunks /* && !isSSL(ctx) */) {
			setHeader(HttpHeaders.Names.TRANSFER_ENCODING, HttpHeaders.Values.CHUNKED);
		}
		if (HttpHeaders.isKeepAlive(request)) {
			setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
		}

		
		setHeader(HttpHeaders.Names.ACCEPT_RANGES, HttpHeaders.Values.BYTES);
		setHeaderContentType(file);
		setHeaderContentDisposition(file, mimeHelper.isBinary(file));
		setDateAndCacheHeaders(file);		
		
		final RandomAccessFile raf;
		try {
			raf = new RandomAccessFile(file, "r");
			long fileLength = raf.length();

			Tuple<Long, Long> range;
			try {
				range = parseRange(fileLength);
			} catch (IllegalArgumentException e) {
				throw new RestException(HttpResponseStatus.REQUESTED_RANGE_NOT_SATISFIABLE, e);
			}

			if (range == null) {
				setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(fileLength));
			} else {
				status = HttpResponseStatus.PARTIAL_CONTENT;
				setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(range.getSecond() - range.getFirst() + 1));
				setHeader(HttpHeaders.Names.CONTENT_RANGE, "bytes " + range.getFirst() + "-" + range.getSecond() + "/" + fileLength);
			}

			HttpResponse response = new DefaultHttpResponse(version, status);
			setupResponseHeaders(response);
			ctx.write(response);

			// write content
			long rangeStart = 0;
			long rangeEnd = fileLength;
			if (range != null) {
				rangeStart = range.getFirst();
				rangeEnd = range.getSecond() - range.getFirst() + 1;
			}
			
			if (isSSL(ctx)) {
				ctx.write(new ChunkedFile(raf, rangeStart, rangeEnd, HTTP_CHUNK_SIZE));
			} if (supportChunks) {
				ctx.write(new ChunkedInputAdapter(new ChunkedFile(raf, rangeStart, rangeEnd, HTTP_CHUNK_SIZE)));
			} else {
				ctx.write(new DefaultFileRegion(raf.getChannel(), rangeStart, rangeEnd));
			}
			
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
			
		} catch (FileNotFoundException e) {
			throw new RestException(HttpResponseStatus.NOT_FOUND, "Not found");
		} catch (IOException e) {
			throw new RestException(HttpResponseStatus.INTERNAL_SERVER_ERROR, "Error");
		}

	}

	private void setHeaderContentType(File file) {
		String mimeType = mimeHelper.getContentType(file);
		setHeader(HttpHeaders.Names.CONTENT_TYPE, mimeType + "; charset=UTF-8");
	}

	private void setHeaderContentDisposition(File file, boolean download) {
		String name = file.getName();
		setHeader("Content-Disposition", (download ? "attachment;" : "inline;") + "filename=\"" + name.replaceAll("[^A-Za-z0-9\\-_\\.]", "_") + "\"");
	}

	private Tuple<Long, Long> parseRange(long availableLength) {
		String header = request.headers().get(HttpHeaders.Names.RANGE);
		if (StringUtils.nullOrEmpty(header)) {
			return null;
		}
		Matcher m = RANGE_HEADER.matcher(header);
		if (!m.matches()) {
			throw new IllegalArgumentException("Unsupported range: " + header);
		}
		Tuple<Long, Long> result = Tuple.empty();
		result.setFirst(Long.parseLong(m.group(1)));
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

	private void setDateAndCacheHeaders(File fileToCache) {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(HttpUtils.HTTP_DATE_FORMAT, Locale.US);
		dateFormatter.setTimeZone(TimeZone.getTimeZone(HttpUtils.HTTP_DATE_GMT_TIMEZONE));

		// Date header
		Calendar time = new GregorianCalendar();
		setHeader(HttpHeaders.Names.DATE, dateFormatter.format(time.getTime()));

		// Add cache headers
		time.add(Calendar.SECOND, HttpUtils.HTTP_CACHE_SECONDS);
		setHeader(HttpHeaders.Names.EXPIRES, dateFormatter.format(time.getTime()));
		setHeader(HttpHeaders.Names.CACHE_CONTROL, "private, max-age=" + HttpUtils.HTTP_CACHE_SECONDS);
		setHeader(HttpHeaders.Names.LAST_MODIFIED, dateFormatter.format(new Date(fileToCache.lastModified())));
	}

	private void setupResponseHeaders(HttpResponse response) {
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			response.headers().set(entry.getKey(), entry.getValue());
		}
	}

	private boolean isSSL(ChannelHandlerContext ctx) {
        return ctx.channel().pipeline().get(SslHandler.class) != null;
    }	
	
}
