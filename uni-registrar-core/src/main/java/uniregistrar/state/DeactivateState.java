package uniregistrar.state;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

@JsonPropertyOrder({ "jobId", "didState", "didRegistrationMetadata", "didDocumentMetadata" })
@JsonIgnoreProperties(ignoreUnknown=true)
public class DeactivateState extends State {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	private DeactivateState(String jobId, Map<String, Object> didState, Map<String, Object> didRegistrationMetadata, Map<String, Object> didDocumentMetadata) {
		super(jobId, didState, didRegistrationMetadata, didDocumentMetadata);
	}

	/*
	 * Factory methods
	 */

	@JsonCreator
	public static DeactivateState build(@JsonProperty(value="jobId", required=false) String jobId, @JsonProperty(value="didState", required=true) Map<String, Object> didState, @JsonProperty(value="didRegistrationMetadata", required=false) Map<String, Object> didRegistrationMetadata, @JsonProperty(value="didDocumentMetadata", required=false) Map<String, Object> didDocumentMetadata) {
		return new DeactivateState(jobId, didState, didRegistrationMetadata, didDocumentMetadata);
	}

	public static DeactivateState build() {
		return new DeactivateState(null, new HashMap<> (), new HashMap<> (), new HashMap<> ());
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
}
