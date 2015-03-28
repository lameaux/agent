package com.euromoby.rest.handler.file;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.stream.ChunkedFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.ConfigurableMimeFileTypeMap;
import org.springframework.stereotype.Component;

import com.euromoby.agent.Config;
import com.euromoby.model.Tuple;
import com.euromoby.rest.ChunkedInputAdapter;
import com.euromoby.rest.RestException;
import com.euromoby.rest.handler.RestHandlerBase;
import com.euromoby.utils.StringUtils;

@Component
public class FileHandler extends RestHandlerBase {

	public static final String URL = "/files";
	private static final Pattern URL_PATTERN = Pattern.compile("/files/(.+)");
	
	private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");

    public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
    public static final int HTTP_CACHE_SECONDS = 24 * 60 * 60; // 1 day	
    
    private static final int HTTP_CHUNK_SIZE = 8192;
    
    private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    private static final Pattern RANGE_HEADER = Pattern.compile("bytes=(\\d+)\\-(\\d+)?");
    
	private static ConfigurableMimeFileTypeMap mimeTypesMap = new ConfigurableMimeFileTypeMap();
	static {
		mimeTypesMap.setMappings(new String[]{
				"video/mp4  mp4"
		});
	}
    
	private Config config;

	@Autowired
	public FileHandler(Config config) {
		this.config = config;
	}

	@Override
	public boolean matchUri(URI uri) {
		Matcher m = URL_PATTERN.matcher(uri.getPath());
		return m.matches();
	}

	@Override
	public void doGetChunked(ChannelHandlerContext ctx) throws Exception {
		URI uri = new URI(request.getUri());

		Matcher m = URL_PATTERN.matcher(uri.getPath());
		if (!m.matches()) {
			throw new RestException(HttpResponseStatus.NOT_FOUND, "Not found");
		}
		String fileUri = m.group(1);
		
		File targetFile = getTargetFile(fileUri);
		
		if (targetFile == null) {
			// TODO ask other agents if they have the file
			throw new RestException(HttpResponseStatus.NOT_FOUND, "Not found");
		}
		
        // Cache Validation
        String ifModifiedSince = request.headers().get(HttpHeaders.Names.IF_MODIFIED_SINCE);
        if (!StringUtils.nullOrEmpty(ifModifiedSince)) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
            Date ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);

            // Only compare up to the second because the datetime format we send to the client
            // does not have milliseconds
            long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
            long fileLastModifiedSeconds = targetFile.lastModified() / 1000;
            if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
            	sendNotModified(ctx);
            	return;
            }
        }		
		
        RandomAccessFile raf = null;
        try {
        	raf = new RandomAccessFile(targetFile, "r");
        	
            long fileLength = raf.length();        
            
    		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
    		response.headers().set(HttpHeaders.Names.ACCEPT_RANGES, HttpHeaders.Values.BYTES);
    		response.headers().set(HttpHeaders.Names.CONTENT_ENCODING, HttpHeaders.Values.IDENTITY);

    		response.headers().set(HttpHeaders.Names.TRANSFER_ENCODING, HttpHeaders.Values.CHUNKED);    		
    		
    		Tuple<Long, Long> range;
            try {
                range = parseRange(fileLength);
            } catch (IllegalArgumentException e) {
                throw new RestException(HttpResponseStatus.REQUESTED_RANGE_NOT_SATISFIABLE, e);
            }
    		
            if (range == null) {
            	HttpHeaders.setContentLength(response, fileLength);
            } else {
	        	response.setStatus(HttpResponseStatus.PARTIAL_CONTENT);
            	HttpHeaders.setContentLength(response, range.getSecond() - range.getFirst() + 1);
            	response.headers().set(HttpHeaders.Names.CONTENT_RANGE,
                          "bytes " + range.getFirst() + "-" + range.getSecond() + "/" + fileLength);
	        }
   		
    		setContentTypeHeader(response, targetFile);
            setDateAndCacheHeaders(response, targetFile);
            if (HttpHeaders.isKeepAlive(request)) {
                response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            }		
            ctx.write(response);        	
        	
            // Write the content.
            ChannelFuture sendFileFuture;
            ChannelFuture lastContentFuture;
            
            long rangeStart = 0;
            long rangeEnd = fileLength;
            if (range != null) {
            	rangeStart = range.getFirst();
            	rangeEnd = range.getSecond() - range.getFirst() + 1;
            }

            lastContentFuture = ctx.write(new ChunkedInputAdapter(new ChunkedFile(raf, rangeStart, rangeEnd, HTTP_CHUNK_SIZE)));            

            ChannelFuture writeFuture = ctx.writeAndFlush(DefaultLastHttpContent.EMPTY_LAST_CONTENT);            
            
            //
//            if (ctx.pipeline().get(SslHandler.class) == null) {
//                sendFileFuture = ctx.write(new DefaultFileRegion(raf.getChannel(), rangeStart, rangeEnd), ctx.newProgressivePromise());
//                // Write the end marker.
//                lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
//            } else {
//            	
//                sendFileFuture = ctx.write(new ChunkedInputAdapter(new ChunkedFile(raf, rangeStart, rangeEnd, HTTP_CHUNK_SIZE)), ctx.newProgressivePromise());
//                // HttpChunkedInput will write the end marker (LastHttpContent) for us.
//                lastContentFuture = sendFileFuture;
//            }        	
        	
            //if (!HttpHeaders.isKeepAlive(request)) {
            //    lastContentFuture.addListener(ChannelFutureListener.CLOSE);
            //}        	
        	
        } catch (FileNotFoundException ignore) {
        	throw new RestException(HttpResponseStatus.NOT_FOUND, "Not found");
        } finally {
        	IOUtils.closeQuietly(raf);
        }
        
	}

	@Override
	public boolean isChunkedResponse() {
		return true;
	}

    private void sendNotModified(ChannelHandlerContext ctx) {
        FullHttpResponse response = createHttpResponse(HttpResponseStatus.NOT_MODIFIED);
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");        
        setDateHeader(response);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }	
	
	private File getTargetFile(String uri) throws RestException {
		// Decode the path.
		try {
			uri = URLDecoder.decode(uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RestException(HttpResponseStatus.NOT_FOUND, "Not found");
		}

		if (uri.isEmpty()) {
			throw new RestException(HttpResponseStatus.NOT_FOUND, "Not found");
		}

		// Convert file separators.
		uri = uri.replace('/', File.separatorChar);

		// TODO Security check.
		if (uri.contains(File.separator + '.') || uri.contains('.' + File.separator) || uri.charAt(0) == '.' || uri.charAt(uri.length() - 1) == '.'
				|| INSECURE_URI.matcher(uri).matches()) {
			throw new RestException(HttpResponseStatus.NOT_FOUND, "Not found");
		}

		File filesPath = new File(config.getAgentFilesPath());
		File targetFile = new File(filesPath, uri);

		if (!targetFile.exists()) {
			// ask other agents if they have the file 
			return null;
		}

		if (!targetFile.isFile()) {
			throw new RestException(HttpResponseStatus.FORBIDDEN, "Forbidden");
		}
		
		if (!targetFile.getAbsolutePath().startsWith(filesPath.getAbsolutePath())) {
			throw new RestException(HttpResponseStatus.NOT_FOUND, "Not found");
		}
		
		return targetFile;
	}
	

    private static void setDateHeader(FullHttpResponse response) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        Calendar time = new GregorianCalendar();
        response.headers().set(HttpHeaders.Names.DATE, dateFormatter.format(time.getTime()));
    }	

    private static void setDateAndCacheHeaders(HttpResponse response, File fileToCache) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        // Date header
        Calendar time = new GregorianCalendar();
        response.headers().set(HttpHeaders.Names.DATE, dateFormatter.format(time.getTime()));

        // Add cache headers
        time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
        response.headers().set(HttpHeaders.Names.EXPIRES, dateFormatter.format(time.getTime()));
        response.headers().set(HttpHeaders.Names.CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);
        response.headers().set(
        		HttpHeaders.Names.LAST_MODIFIED, dateFormatter.format(new Date(fileToCache.lastModified())));
    }    
    
	private static void setContentTypeHeader(HttpResponse response, File file) {
		String mimeType = mimeTypesMap.getContentType(file);
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE, mimeType);
		setContentDisposition(response, file.getName(), APPLICATION_OCTET_STREAM.equals(mimeType));
	}

	private static void setContentDisposition(HttpResponse response, String name, boolean download) {
		response.headers().set("Content-Disposition",
				(download ? "attachment;" : "inline;") + "filename=\"" + name.replaceAll("[^A-Za-z0-9\\-_\\.]", "_") + "\"");
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
        Tuple<Long, Long> result = new Tuple<Long, Long>();
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
	
}
