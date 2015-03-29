package com.euromoby.rest.handler.file;

import java.io.File;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.Config;

@Component
public class FileProvider {

	private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");

	private Config config;

	@Autowired
	public FileProvider(Config config) {
		this.config = config;
	}

	public File getFileByLocation(String location) {
		if (location.isEmpty()) {
			return null;
		}
		// Convert file separators.
		location = location.replace('/', File.separatorChar);

		// check for invalid characters in file location
		if (location.contains(File.separator + '.') || location.contains('.' + File.separator) || location.charAt(0) == '.'
				|| location.charAt(location.length() - 1) == '.' || INSECURE_URI.matcher(location).matches()) {
			return null;
		}

		File filesPath = new File(config.getAgentFilesPath());
		File targetFile = new File(filesPath, location);

		// check that file is from allowed location
		if (!targetFile.getAbsolutePath().startsWith(filesPath.getAbsolutePath())) {
			return null;
		}
		// only files are supported
		if (!targetFile.exists() || !targetFile.isFile()) {
			return null;
		}
		return targetFile;
	}

}
