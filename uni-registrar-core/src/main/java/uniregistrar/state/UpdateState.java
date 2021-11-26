package uniregistrar.state;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonPropertyOrder({ "jobId", "didState", "didRegistrationMetadata", "didDocumentMetadata" })
@JsonIgnoreProperties(ignoreUnknown=true)
public class UpdateState {

	public static final String MIME_TYPE = "application/json";

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@JsonProperty
	private String jobId;

	@JsonProperty
	private Map<String, Object> didState;

	@JsonProperty
	private Map<String, Object> didRegistrationMetadata;

	@JsonProperty
	private Map<String, Object> didDocumentMetadata;

	private UpdateState(String jobId, Map<String, Object> didState, Map<String, Object> didRegistrationMetadata, Map<String, Object> didDocumentMetadata) {
		this.jobId = jobId;
		this.didState = didState;
		this.didRegistrationMetadata = didRegistrationMetadata;
		this.didDocumentMetadata = didDocumentMetadata;
	}

	/*
	 * Factory methods
	 */

	@JsonCreator
	public static UpdateState build(@JsonProperty(value="jobId", required=false) String jobId, @JsonProperty(value="didState", required=true) Map<String, Object> didState, @JsonProperty(value="didRegistrationMetadata", required=false) Map<String, Object> didRegistrationMetadata, @JsonProperty(value="didDocumentMetadata", required=false) Map<String, Object> didDocumentMetadata) {
		return new UpdateState(jobId, didState, didRegistrationMetadata, didDocumentMetadata);
	}

	public static UpdateState build() {
		return new UpdateState(null, new HashMap<String, Object> (), new HashMap<String, Object> (), new HashMap<String, Object> ());
	}

	/*
	 * Serialization
	 */

	public static UpdateState fromJson(String json) throws JsonParseException, JsonMappingException, IOException {
		return objectMapper.readValue(json, UpdateState.class);
	}

	public static UpdateState fromJson(Reader reader) throws JsonParseException, JsonMappingException, IOException {
		return objectMapper.readValue(reader, UpdateState.class);
	}

	public String toJson() throws JsonProcessingException {
		return objectMapper.writeValueAsString(this);
	}

	/*
	 * Static methods
	 */

	@JsonIgnore
	public static String getState(CreateState createState) {
		return (String) createState.getDidState().get("state");
	}

	@JsonIgnore
	public static void setState(CreateState createState, String state) {
		createState.getDidState().put("state", state);
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
	public final Map<String, Object> getDidRegistrationMetadata() {
		return this.didRegistrationMetadata;
	}

	@JsonSetter
	public final void setDidRegistrationMetadata(Map<String, Object> didRegistrationMetadata) {
		this.didRegistrationMetadata = didRegistrationMetadata;
	}

	@JsonGetter
	public final Map<String, Object> getDidDocumentMetadata() {
		return this.didDocumentMetadata;
	}

	@JsonSetter
	public final void setDidDocumentMetadata(Map<String, Object> didDocumentMetadata) {
		this.didDocumentMetadata = didDocumentMetadata;
	}

	/*
	 * Object methods
	 */

	public String toString() {
		try {
			return this.toJson();
		} catch (JsonProcessingException ex) {
			return ex.getMessage();
		}
	}
}
