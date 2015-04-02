package com.euromoby.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.euromoby.job.JobDetail;
import com.euromoby.job.JobManager;

@RunWith(MockitoJUnitRunner.class)
public class DownloadCommandTest {

	private static final String URL = "http://example.com";
	private static final String LOCATION = "file";

	@Mock
	JobManager jobManager;
	DownloadCommand downloadCommand;

	@Before
	public void init() {
		downloadCommand = new DownloadCommand(jobManager);
	}

	@Test
	public void testMatchName() {
		assertTrue(downloadCommand.match(DownloadCommand.NAME));
	}

	@Test
	public void testMatchWithParam() {
		assertTrue(downloadCommand.match(DownloadCommand.NAME + Command.SEPARATOR + URL + Command.SEPARATOR + LOCATION));
		assertTrue(downloadCommand.match(DownloadCommand.NAME + Command.SEPARATOR + URL + Command.SEPARATOR + LOCATION + Command.SEPARATOR
				+ DownloadCommand.NO_PROXY));
	}

	@Test
	public void testNotMatch() {
		assertFalse(downloadCommand.match(DownloadCommand.NAME + "aaa"));
	}

	@Test
	public void testBadRequest() {
		String result = downloadCommand.execute(DownloadCommand.NAME + Command.SEPARATOR + "aaa");
		assertEquals(downloadCommand.syntaxError(), result);
	}

	@Test
	public void testWithProxy() {
		String result = downloadCommand.execute(downloadCommand.name() + Command.SEPARATOR + URL + Command.SEPARATOR + LOCATION);
		Mockito.verify(jobManager).submit(Matchers.any(JobDetail.class));
		assertFalse(downloadCommand.syntaxError().equals(result));
	}

	@Test
	public void testWithoutProxy() {
		String result = downloadCommand.execute(downloadCommand.name() + Command.SEPARATOR + URL + Command.SEPARATOR + LOCATION + Command.SEPARATOR + DownloadCommand.NO_PROXY);
		Mockito.verify(jobManager).submit(Matchers.any(JobDetail.class));
		assertFalse(downloadCommand.syntaxError().equals(result));
	}	
}
