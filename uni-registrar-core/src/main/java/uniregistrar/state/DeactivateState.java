package uniregistrar.state;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

@JsonPropertyOrder({ "jobId", "didState", "didRegistrationMetadata", "didDocumentMetadata" })
@JsonIgnoreProperties(ignoreUnknown=true)
public class DeactivateState extends State {

	private DeactivateState(String jobId, Map<String, Object> didState, Map<String, Object> didRegistrationMetadata, Map<String, Object> didDocumentMetadata) {
		super(jobId, didState, didRegistrationMetadata, didDocumentMetadata);
	}

	private DeactivateState() {
		this(null, new HashMap<> (), new HashMap<> (), new HashMap<> ());
	}

	/*
	 * Factory methods
	 */

	@JsonCreator
	public static DeactivateState build(@JsonProperty(value="jobId") String jobId, @JsonProperty(value="didState", required=true) Map<String, Object> didState, @JsonProperty(value="didRegistrationMetadata") Map<String, Object> didRegistrationMetadata, @JsonProperty(value="didDocumentMetadata") Map<String, Object> didDocumentMetadata) {
		return new DeactivateState(jobId, didState, didRegistrationMetadata, didDocumentMetadata);
	}

	public static DeactivateState build() {
		return new DeactivateState();
	}

	/*
	 * Serialization
	 */

	public static DeactivateState fromJson(String json) throws JsonParseException, JsonMappingException, IOException {
		return objectMapper.readValue(json, DeactivateState.class);
	}

	public static DeactivateState fromJson(Reader reader) throws JsonParseException, JsonMappingException, IOException {
		return objectMapper.readValue(reader, DeactivateState.class);
	}

	public static DeactivateState fromMap(Map<String, Object> map) {
		return objectMapper.convertValue(map, DeactivateState.class);
	}
}
