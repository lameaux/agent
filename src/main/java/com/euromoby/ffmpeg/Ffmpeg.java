package com.euromoby.ffmpeg;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.agent.Config;
import com.euromoby.ffmpeg.model.FfmpegFormat;
import com.euromoby.ffmpeg.model.FfmpegOutput;
import com.euromoby.utils.ShellExecutor;
import com.euromoby.utils.StringUtils;
import com.google.gson.Gson;

@Component
public class Ffmpeg {

	public static final String FFMPEG = "ffmpeg";
	public static final String FFPROBE = "ffprobe";
	public static final int TIMEOUT = 60 * 1000;
	
	private static final Gson gson = new Gson();
	
	private ShellExecutor shellExecutor;
	private Config config;
	
	@Autowired
	public Ffmpeg(ShellExecutor shellExecutor, Config config) {
		this.shellExecutor = shellExecutor;
		this.config = config;
	}

	protected String getPathToCommand(String command) {
		String ffmpegLocation = config.getFfmpegPath();
		if (StringUtils.nullOrEmpty(ffmpegLocation)) {
			return command;
		}		
		return ffmpegLocation + File.separatorChar + command;
	}
	
	protected String[] getFormatCommand(String fileName) {
		List<String> command = new ArrayList<String>();
		command.add(getPathToCommand(FFPROBE));
		command.add("-loglevel");
		command.add("error");
		command.add("-show_format");
		command.add(fileName);
		command.add("-print_format");		
		command.add("json");	
		return command.toArray(new String[]{});
	}

	protected String[] getThumbnailCommand(String inFileName, int offsetSeconds, int width, int height, String outFileName) {
		List<String> command = new ArrayList<String>();
		command.add(getPathToCommand(FFMPEG));
		command.add("-y"); // do not ask
		command.add("-loglevel");
		command.add("error");
		command.add("-ss");
		command.add(String.valueOf(offsetSeconds));
		command.add("-i");
		command.add(inFileName);		
		command.add("-vframes");	
		command.add("1");
		command.add("-vf");		
		command.add("scale=" + width + ":" + height);	
		command.add(outFileName);		
		return command.toArray(new String[]{});
	}	
	
	public FfmpegFormat getFormat(String fileName) throws Exception {
		String[] commandLine = getFormatCommand(fileName);
		String shellOutput = shellExecutor.executeCommandLine(commandLine, TIMEOUT);
		FfmpegOutput ffmpegOutput = gson.fromJson(shellOutput, FfmpegOutput.class);
		if (ffmpegOutput == null) {
			throw new Exception("Wrong output");
		}
		return ffmpegOutput.getFormat();
	}
	
	public void createThumbnail(String inputFileName, int offsetSeconds, int width, int height, String outputFileName) throws Exception {
		String[] commandLine = getThumbnailCommand(inputFileName, offsetSeconds, width, height, outputFileName);
		shellExecutor.executeCommandLine(commandLine, TIMEOUT);
	}
	
}
