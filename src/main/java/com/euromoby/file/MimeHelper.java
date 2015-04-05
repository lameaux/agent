package com.euromoby.file;

import java.io.File;

import org.springframework.mail.javamail.ConfigurableMimeFileTypeMap;
import org.springframework.stereotype.Component;

@Component
public class MimeHelper {

    private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";	
    
    private static final String CONTENT_TEXT = "text/";
    
	private ConfigurableMimeFileTypeMap mimeTypesMap = new ConfigurableMimeFileTypeMap();
	
	public MimeHelper() {
		mimeTypesMap.setMappings(new String[]{
				"video/mp4  mp4"
		});		
	}

	public String getContentType(File file) {
		return mimeTypesMap.getContentType(file);
	}
	
	public boolean isBinary(File file) {
		return APPLICATION_OCTET_STREAM.equals(getContentType(file));
	}

	public boolean isCompressible(File file) {
		return getContentType(file).contains(CONTENT_TEXT);
	}	
	
}
