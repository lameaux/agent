package com.euromoby.ffmpeg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.euromoby.agent.Config;
import com.euromoby.ffmpeg.model.FfmpegFormat;
import com.euromoby.utils.ShellExecutor;

@Ignore
public class FfmpegIntegrationTest {

	public static final String FFMPEG_LOCATION = "c:\\ffmpeg\\bin";
	public static final String INPUT_FILENAME = "c:\\agenttest\\video1.mp4";
	public static final String OUTPUT_FILENAME = "c:\\agenttest\\thumb1.jpg";
	
	Ffmpeg ffmpeg;
	
	@Before
	public void init() {
		ShellExecutor shellExecutor = new ShellExecutor();
		ffmpeg = new Ffmpeg(shellExecutor, new Config(){
			@Override
			public String getFfmpegPath() {
				return FFMPEG_LOCATION;
			} 
		});
	}

	@Test
	public void testGetFormat() throws Exception {
		FfmpegFormat ffmpegFormat = ffmpeg.getFormat(INPUT_FILENAME);
		assertEquals(87, Math.round(ffmpegFormat.getDuration()));
	}

	@Test
	public void testGetThumbnail() throws Exception {
		File out = new File(OUTPUT_FILENAME);
		out.delete();
		ffmpeg.createThumbnail(INPUT_FILENAME, 20, 320, -1, OUTPUT_FILENAME);
		assertTrue(out.exists());
	}	
	
}
