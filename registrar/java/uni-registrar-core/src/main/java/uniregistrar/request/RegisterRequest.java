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

public class RegisterRequest {

	public static final String MIME_TYPE = "application/json";

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@JsonProperty
	private String jobId;

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

	public RegisterRequest() {

	}

	public RegisterRequest(String jobId, Map<String, Object> options, Map<String, Object> secret, List<Service> addServices, List<PublicKey> addPublicKeys, List<Authentication> addAuthentications) {

		this.jobId = jobId;
		this.options = options;
		this.secret = secret;
		this.addServices = addServices;
		this.addPublicKeys = addPublicKeys;
		this.addAuthentications = addAuthentications;
	}

	/*
	 * Serialization
	 */

	public static RegisterRequest fromJson(String json) throws JsonParseException, JsonMappingException, IOException {

		return objectMapper.readValue(json, RegisterRequest.class);
	}

	public static RegisterRequest fromJson(Reader reader) throws JsonParseException, JsonMappingException, IOException {

		return objectMapper.readValue(reader, RegisterRequest.class);
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

	@JsonGetter
	public List<Service> getAddServices() {

		return this.addServices;
	}

	@JsonSetter
	public void setAddServices(List<Service> addServices) {

		this.addServices = addServices;
	}

	@JsonGetter
	public List<PublicKey> getAddPublicKeys() {

		return this.addPublicKeys;
	}

	@JsonSetter
	public void setAddPublicKeys(List<PublicKey> addPublicKeys) {

		this.addPublicKeys = addPublicKeys;
	}

	@JsonGetter
	public List<Authentication> getAddAuthentications() {

		return this.addAuthentications;
	}

	@JsonSetter
	public void setAddAuthentications(List<Authentication> addAuthentications) {

		this.addAuthentications = addAuthentications;
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
