package uniregistrar.state;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class RegisterStateAction extends RegisterState {

	public RegisterStateAction(String jobId, Map<String, Object> registrarMetadata, Map<String, Object> methodMetadata, String action) {

		super(jobId, registrarMetadata, methodMetadata, "action");

		this.setAction(action);
	}

	/*
	 * Getters and setters
	 */

	@JsonIgnore
	public final String getAction() {

		return this.getDidState() == null ? null : (String) this.getDidState().get("action");
	}

	@JsonIgnore
	public final void setAction(String action) {

		if (this.getDidState() != null && action != null) this.getDidState().put("action", action);
	}
}
