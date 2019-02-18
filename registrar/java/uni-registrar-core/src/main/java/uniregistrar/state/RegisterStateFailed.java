package uniregistrar.state;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class RegisterStateFailed extends RegisterState {

	public RegisterStateFailed(String jobId, Map<String, Object> registrarMetadata, Map<String, Object> methodMetadata, String reason) {

		super(jobId, registrarMetadata, methodMetadata, "failed");

		this.setReason(reason);
	}

	/*
	 * Getters and setters
	 */

	@JsonIgnore
	public final String getReason() {

		return this.getDidState() == null ? null : (String) this.getDidState().get("reason");
	}

	@JsonIgnore
	public final void setReason(String reason) {

		if (this.getDidState() != null && reason != null) this.getDidState().put("reason", reason);
	}
}
