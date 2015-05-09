package com.euromoby.job;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.euromoby.download.DownloadClient;
import com.euromoby.ffmpeg.Ffmpeg;
import com.euromoby.file.FileProvider;
import com.euromoby.upload.UploadClient;
import com.euromoby.upload.UploadJob;
import com.euromoby.video.GrabVideoJob;

@RunWith(MockitoJUnitRunner.class)
public class JobFactoryTest {

	@Mock
	UploadClient uploadClient;
	@Mock
	DownloadClient downloadClient;
	@Mock
	GetJobsClient getJobsClient;
	@Mock	
	FileProvider fileProvider; 
	@Mock	
	Ffmpeg ffmpeg;	
	
	JobFactory jobFactory;

	@Before
	public void init() throws Exception {
		jobFactory = new JobFactory();
		jobFactory.setUploadClient(uploadClient);
		jobFactory.setDownloadClient(downloadClient);
		jobFactory.setGetJobsClient(getJobsClient);
		jobFactory.setFfmpeg(ffmpeg);
		jobFactory.setFileProvider(fileProvider);
	}	
	
	@SuppressWarnings("rawtypes")
	@Test
	public void testGetJobClasses() {
		Class[] classes = jobFactory.getJobClasses();
		assertTrue(classes.length > 0);
		for (Class clazz : classes) {
			assertTrue(Job.class.isAssignableFrom(clazz));
		}
	}

	@Test
	public void shouldCreateUploadJob() throws Exception {
		JobDetail jobDetail = new JobDetail(UploadJob.class);
		Job job = jobFactory.createJob(jobDetail);
		assertNotNull(job);
		assertTrue(job instanceof UploadJob);
	}	

	@Test
	public void shouldCreateGrabVideoJob() throws Exception {
		JobDetail jobDetail = new JobDetail(GrabVideoJob.class);
		Job job = jobFactory.createJob(jobDetail);
		assertNotNull(job);
		assertTrue(job instanceof GrabVideoJob);
	}	

	@Test
	public void shouldCreateGetNewJobsJob() throws Exception {
		JobDetail jobDetail = new JobDetail(GetNewJobsJob.class);
		Job job = jobFactory.createJob(jobDetail);
		assertNotNull(job);
		assertTrue(job instanceof GetNewJobsJob);
	}	
	
	@Test
	public void shouldThrowException() throws Exception {
		JobDetail jobDetail = new JobDetail(Job.class);
		try {
			jobFactory.createJob(jobDetail);
			fail();
		} catch (Exception e) {}
	}	
	
}
