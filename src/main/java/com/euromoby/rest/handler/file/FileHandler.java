package com.euromoby.rest.handler.file;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.file.FileProvider;
import com.euromoby.file.MimeHelper;
import com.euromoby.rest.RestException;
import com.euromoby.rest.handler.RestHandlerBase;
import com.euromoby.utils.StringUtils;

@Component
public class FileHandler extends RestHandlerBase {

	public static final String URL = "/files";
	private static final Pattern URL_PATTERN = Pattern.compile(URL + "/(.+)");

    public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
    public static final int HTTP_CACHE_SECONDS = 24 * 60 * 60; // 1 day	

	private FileProvider fileProvider;
	private MimeHelper mimeHelper;

	@Autowired
	public FileHandler(FileProvider fileProvider, MimeHelper mimeHelper) {
		this.fileProvider = fileProvider;
		this.mimeHelper = mimeHelper;
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
		String fileLocation = m.group(1);

		try {
			fileLocation = URLDecoder.decode(fileLocation, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RestException("Invalid request");
		}		
		
		File targetFile = fileProvider.getFileByLocation(fileLocation);
		if (targetFile == null) {
			throw new RestException(HttpResponseStatus.NOT_FOUND, "Not found");
		}
		
        // Cache Validation
		if (!ifModifiedSince(targetFile)) {
        	sendNotModified(ctx);
        	return;			
		}
		
		FileResponse fileResponse = new FileResponse(request, mimeHelper);
		fileResponse.send(ctx, targetFile);
	}

	@Override
	public boolean isChunkedResponse() {
		return true;
	}

	private boolean ifModifiedSince(File file) {

        String ifModifiedSince = request.headers().get(HttpHeaders.Names.IF_MODIFIED_SINCE);
        if (!StringUtils.nullOrEmpty(ifModifiedSince)) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
            Date ifModifiedSinceDate;
			try {
				ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);
			} catch (ParseException e) {
				return true;
			}

            // Only compare up to the second
            long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
            long fileLastModifiedSeconds = file.lastModified() / 1000;
            if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
            	return false;
            }
        }		
		return true;
	}
	
    private void sendNotModified(ChannelHandlerContext ctx) {
        FullHttpResponse response = createHttpResponse(HttpResponseStatus.NOT_MODIFIED);
		response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");        
        setDateHeader(response);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }	

    private static void setDateHeader(FullHttpResponse response) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        Calendar time = new GregorianCalendar();
        response.headers().set(HttpHeaders.Names.DATE, dateFormatter.format(time.getTime()));
    }	

}
