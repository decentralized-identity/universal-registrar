package uniregistrar.request;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import did.Authentication;
import did.DIDDocument;
import did.PublicKey;
import did.Service;

public class UpdateRequest {

	public static final String MIME_TYPE = "application/json";

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@JsonProperty
	private String jobId;

	@JsonProperty
	private String identifier;

	@JsonProperty
	private Map<String, Object> options;

	@JsonProperty
	private Map<String, Object> secret;

	@JsonProperty
	private DIDDocument didDocument;

	@JsonProperty
	private List<Service> addServices;

	@JsonProperty
	private List<PublicKey> addPublicKeys;

	@JsonProperty
	private List<Authentication> addAuthentications;

	public UpdateRequest() {

	}

	public UpdateRequest(String jobId, Map<String, Object> options, Map<String, Object> secret) {

		this.jobId = jobId;
		this.options = options;
		this.secret = secret;
	}

	/*
	 * Serialization
	 */

	public static UpdateRequest fromJson(String json) throws JsonParseException, JsonMappingException, IOException {

		return objectMapper.readValue(json, UpdateRequest.class);
	}

	public static UpdateRequest fromJson(Reader reader) throws JsonParseException, JsonMappingException, IOException {

		return objectMapper.readValue(reader, UpdateRequest.class);
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
	public final Map<String, Object> getOptions() {

		return this.options;
	}

	@JsonSetter
	public final void setOptions(Map<String, Object> options) {

		this.options = options;
	}

	@JsonGetter
	public final Map<String, Object> getSecret() {

		return this.secret;
	}

	@JsonSetter
	public final void setSecret(Map<String, Object> secret) {

		this.secret = secret;
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
