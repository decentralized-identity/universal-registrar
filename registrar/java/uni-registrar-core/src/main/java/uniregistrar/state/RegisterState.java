package uniregistrar.state;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RegisterState {

	public static final String MIME_TYPE = "application/json";

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@JsonProperty
	private String jobId;

	@JsonProperty
	private Map<String, Object> didState;

	@JsonProperty
	private Map<String, Object> registrarMetadata;

	@JsonProperty
	private Map<String, Object> methodMetadata;

	public RegisterState() {

	}

	public RegisterState(String jobId, Map<String, Object> registrarMetadata, Map<String, Object> methodMetadata, String state) {

		this.jobId = jobId;
		this.didState = new HashMap<String, Object> ();
		this.registrarMetadata = registrarMetadata;
		this.methodMetadata = methodMetadata;

		this.setState(state);
	}

	/*
	 * Serialization
	 */

	public static RegisterState fromJson(String json) throws JsonParseException, JsonMappingException, IOException {

		return objectMapper.readValue(json, RegisterState.class);
	}

	public static RegisterState fromJson(Reader reader) throws JsonParseException, JsonMappingException, IOException {

		return objectMapper.readValue(reader, RegisterState.class);
	}

	public String toJson() throws JsonProcessingException {

		return objectMapper.writeValueAsString(this);
	}

	/*
	 * Getters and setters
	 */

	@JsonGetter
	public final String getJobId() {

		return this.jobId;
	}

	@JsonSetter
	public final void setJobId(String jobId) {

		this.jobId = jobId;
	}

	@JsonGetter
	public final Map<String, Object> getDidState() {

		return this.didState;
	}

	@JsonSetter
	public final void setDidState(Map<String, Object> didState) {

		this.didState = didState;
	}

	@JsonGetter
	public final Map<String, Object> getRegistrarMetadata() {

		return this.registrarMetadata;
	}

	@JsonSetter
	public final void setRegistrarMetadata(Map<String, Object> registrarMetadata) {

		this.registrarMetadata = registrarMetadata;
	}

	@JsonGetter
	public final Map<String, Object> getMethodMetadata() {

		return this.methodMetadata;
	}

	@JsonSetter
	public final void setMethodMetadata(Map<String, Object> methodMetadata) {

		this.methodMetadata = methodMetadata;
	}

	@JsonIgnore
	public final String getState() {

		return this.getDidState() == null ? null : (String) this.getDidState().get("state");
	}

	@JsonIgnore
	public final void setState(String state) {

		if (this.getDidState() != null && state != null) this.getDidState().put("state", state);
	}

	/*
	 * Object methods
	 */

	public String toString() {

		try {

			return this.toJson();
		} catch (JsonProcessingException ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
}
