package uniregistrar.driver;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractDriver implements Driver {

	private Map<String, Job> jobs = new HashMap<String, Job> ();

	protected String newJob(Job job) {

		String jobId = UUID.randomUUID().toString();
		this.jobs.put(jobId, job);

		return jobId;
	}

	protected Job continueJob(String jobId) {

		return this.jobs.get(jobId);
	}

	protected void finishJob(String jobId) {

		this.jobs.remove(jobId);
	}

	protected interface Job {

	}
}
