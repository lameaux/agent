package com.euromoby.file;

import java.io.File;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.Config;

@Component
public class FileProvider {

	private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");
	public static final String DEFAULT_EXTENSION = ".agentfile.html";
	public static final String INDEX_NAME = "index";	
	public static final String REGEX_FILE_WITH_EXT = ".+\\.[^.]+$"; 	

	private Config config;

	@Autowired
	public FileProvider(Config config) {
		this.config = config;
	}

	public File getTargetFile(String location) throws Exception {
		File targetFile = new File(new File(config.getAgentFilesPath()), location);
		File parentDir = targetFile.getParentFile();
		if (!parentDir.exists() && !parentDir.mkdirs()) {
			throw new Exception("Error saving file to " + targetFile.getAbsolutePath());
		}
		
		return targetFile;
	}	
	
	public File getFileByLocation(String location) {
		if (location.isEmpty()) {
			location = INDEX_NAME;
		}
		
		// Convert file separators.
		location = location.replace('/', File.separatorChar);

		if (location.endsWith(File.separator)) {
			location = location + INDEX_NAME;
		}
		
		// check for invalid characters in file location
		if (location.contains(File.separator + '.') || location.contains('.' + File.separator) || location.charAt(0) == '.'
				|| location.charAt(location.length() - 1) == '.' || INSECURE_URI.matcher(location).matches()) {
			return null;
		}

		if (!location.matches(REGEX_FILE_WITH_EXT)) {
			location = location + FileProvider.DEFAULT_EXTENSION;
		}
		
		File filesPath = new File(config.getAgentFilesPath());
		File targetFile = new File(filesPath, location);

		// check that file is from allowed location
		if (!targetFile.getAbsolutePath().startsWith(filesPath.getAbsolutePath())) {
			return null;
		}

		// only files are supported
//		if (!targetFile.exists() && !targetFile.isFile()) {
//			return null;
//		}		
		
		return targetFile;
	}

}
