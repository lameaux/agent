package job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobStatusNotifier {

	private static final Logger LOG = LoggerFactory.getLogger(JobStatusNotifier.class);

	public void notify(JobStatus jobStatus) {
		LOG.info(jobStatus.toString());
	}

}
