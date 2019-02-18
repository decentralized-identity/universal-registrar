package uniregistrar.state;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class RegisterStateWait extends RegisterState {

	public RegisterStateWait(String jobId, Map<String, Object> registrarMetadata, Map<String, Object> methodMetadata, String wait, String waitTime) {

		super(jobId, registrarMetadata, methodMetadata, "wait");

		this.setWait(wait);
		this.setWaitTime(waitTime);
	}

	/*
	 * Getters and setters
	 */

	@JsonIgnore
	public final String getWait() {

		return this.getDidState() == null ? null : (String) this.getDidState().get("wait");
	}

	@JsonIgnore
	public final void setWait(String wait) {

		if (this.getDidState() != null && wait != null) this.getDidState().put("wait", wait);
	}

	@JsonIgnore
	public final String getWaitTime() {

		return this.getDidState() == null ? null : (String) this.getDidState().get("waitTime");
	}

	@JsonIgnore
	public final void setWaitTime(String waitTime) {

		if (this.getDidState() != null && waitTime != null) this.getDidState().put("waitTime", waitTime);
	}
}
