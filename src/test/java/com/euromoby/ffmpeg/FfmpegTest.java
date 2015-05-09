package com.euromoby.ffmpeg;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.euromoby.agent.Config;
import com.euromoby.ffmpeg.model.FfmpegFormat;
import com.euromoby.utils.ShellExecutor;

@RunWith(MockitoJUnitRunner.class)
public class FfmpegTest {

	private static final String FFMPEG_LOCATION = "c:\\ffmpeg";
	private static final String FILENAME = "c:\\video\\video1.mp4";
	private static final String OUTFILENAME = "c:\\thumbnail\\thumb1.jpg";
	private static final int OFFSET = 20;
	private static final int WIDTH = 320;
	private static final int HEIGHT = 240;

	Ffmpeg ffmpeg;

	@Mock
	ShellExecutor shellExecutor;
	@Mock
	Config config;

	@Before
	public void init() {
		Mockito.when(config.getFfmpegPath()).thenReturn(FFMPEG_LOCATION);
		ffmpeg = new Ffmpeg(shellExecutor, config);
	}

	@Test
	public void testGetPathToCommand() {
		assertEquals(FFMPEG_LOCATION + File.separator + Ffmpeg.FFPROBE, ffmpeg.getPathToCommand(Ffmpeg.FFPROBE));
	}

	@Test
	public void testGetFormatCommand() {
		String[] command = { ffmpeg.getPathToCommand(Ffmpeg.FFPROBE), "-loglevel", "error", "-show_format", FILENAME, "-print_format", "json" };
		assertArrayEquals(command, ffmpeg.getFormatCommand(FILENAME));
	}

	@Test
	public void testGetThumbnailCommand() {
		String outfile = "out.jpg";
		String[] command = { ffmpeg.getPathToCommand(Ffmpeg.FFMPEG), "-y", "-loglevel", "error", "-ss", String.valueOf(OFFSET), "-i", FILENAME,
				"-vframes", "1", "-vf", "scale=" + WIDTH + ":" + HEIGHT, outfile };
		assertArrayEquals(command, ffmpeg.getThumbnailCommand(FILENAME, OFFSET, WIDTH, HEIGHT, outfile));
	}

	@Test
	public void testGetFormat() throws Exception {
		String testOutput = getClass().getResource("ffprobe_format.json").getFile();
		String output = FileUtils.readFileToString(new File(testOutput));
		Mockito.when(shellExecutor.executeCommandLine(Matchers.any(String[].class), Matchers.anyInt())).thenReturn(output);
		FfmpegFormat ffmpegFormat = ffmpeg.getFormat(FILENAME);
		assertEquals(87, Math.round(ffmpegFormat.getDuration()));
	}

	@Test
	public void testGetThumbnail() throws Exception {
		ffmpeg.createThumbnail(FILENAME, OFFSET, WIDTH, HEIGHT, OUTFILENAME);
		Mockito.verify(shellExecutor).executeCommandLine(Matchers.any(String[].class), Matchers.anyInt());
	}

}
