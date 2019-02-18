package uniregistrar.state;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class RegisterStateFinished extends RegisterState {

	public RegisterStateFinished(String jobId, Map<String, Object> registrarMetadata, Map<String, Object> methodMetadata, String identifier, Map<String, Object> secret) {

		super(jobId, registrarMetadata, methodMetadata, "finished");

		this.setIdentifier(identifier);
		this.setSecret(secret);
	}

	/*
	 * Getters and setters
	 */

	@JsonIgnore
	public final String getIdentifier() {

		return this.getDidState() == null ? null : (String) this.getDidState().get("identifier");
	}

	@JsonIgnore
	public final void setIdentifier(String identifier) {

		if (this.getDidState() != null && identifier != null) this.getDidState().put("identifier", identifier);
	}

	@JsonIgnore
	@SuppressWarnings("unchecked")
	public final Map<String, Object> getSecret() {

		return this.getDidState() == null ? null : (Map<String, Object>) this.getDidState().get("secret");
	}

	@JsonIgnore
	public final void setSecret(Map<String, Object> secret) {

		if (this.getDidState() != null && secret != null) this.getDidState().put("secret", secret);
	}
}
