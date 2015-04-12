package com.euromoby.download;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.euromoby.file.FileProvider;
import com.euromoby.job.JobDetail;
import com.euromoby.job.JobState;

@RunWith(MockitoJUnitRunner.class)
public class DownloadJobTest {

	private static final String URL = "https://localhost/file.zip";
	private static final String LOCATION = "file.zip";
	private static final boolean NO_PROXY = true;

	@Mock
	DownloadClient downloadClient;
	@Mock
	FileProvider fileProvider;
	
	JobDetail jobDetail;
	DownloadJob downloadJob;


	@Before
	public void init() {
		jobDetail = new JobDetail(DownloadJob.class, new HashMap<String, String>());
		downloadJob = new DownloadJob(jobDetail, downloadClient, fileProvider);
	}

	@Test
	public void testEmptyParameters() throws Exception {
		// empty
		downloadJob.call();
		assertEquals(DownloadJob.ERROR_PARAMS_EMPTY, jobDetail.getMessage());
		assertTrue(jobDetail.isError());
		assertEquals(JobState.FAILED, jobDetail.getState());
	}

	@Test
	public void testUrlParameterMissing() throws Exception {
		// make not empty
		jobDetail.getParameters().put("dummy", "dummy");
		downloadJob.call();
		assertEquals(String.format(DownloadJob.ERROR_PARAM_MISSING, DownloadJob.PARAM_URL), jobDetail.getMessage());
		assertTrue(jobDetail.isError());
		assertEquals(JobState.FAILED, jobDetail.getState());
	}

	@Test
	public void testLocationParameterMissing() throws Exception {
		// add url only
		jobDetail.getParameters().put(DownloadJob.PARAM_URL, URL);
		downloadJob.call();		
		assertEquals(String.format(DownloadJob.ERROR_PARAM_MISSING, DownloadJob.PARAM_LOCATION), jobDetail.getMessage());
		assertTrue(jobDetail.isError());
		assertEquals(JobState.FAILED, jobDetail.getState());
	}

	@Test
	public void testSuccess() throws Exception {
		// add url only
		jobDetail.getParameters().put(DownloadJob.PARAM_URL, URL);
		jobDetail.getParameters().put(DownloadJob.PARAM_LOCATION, LOCATION);
		jobDetail.getParameters().put(DownloadJob.PARAM_NOPROXY, String.valueOf(NO_PROXY));
		File fileLocation = new File(LOCATION);
		Mockito.when(fileProvider.getTargetFile(LOCATION)).thenReturn(fileLocation);
		downloadJob.call();		
		Mockito.verify(downloadClient).download(Matchers.eq(URL), Matchers.eq(fileLocation), Matchers.eq(NO_PROXY));
		assertFalse(jobDetail.isError());
		assertEquals(JobState.FINISHED, jobDetail.getState());
	}	
	
}
