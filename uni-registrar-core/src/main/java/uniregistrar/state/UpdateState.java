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
public class UpdateState extends State {

	private UpdateState(String jobId, Map<String, Object> didState, Map<String, Object> didRegistrationMetadata, Map<String, Object> didDocumentMetadata) {
		super(jobId, didState, didRegistrationMetadata, didDocumentMetadata);
	}

	/*
	 * Factory methods
	 */

	@JsonCreator
	public static UpdateState build(@JsonProperty(value="jobId", required=false) String jobId, @JsonProperty(value="didState", required=true) Map<String, Object> didState, @JsonProperty(value="didRegistrationMetadata", required=false) Map<String, Object> didRegistrationMetadata, @JsonProperty(value="didDocumentMetadata", required=false) Map<String, Object> didDocumentMetadata) {
		return new UpdateState(jobId, didState, didRegistrationMetadata, didDocumentMetadata);
	}

	public static UpdateState build() {
		return new UpdateState(null, new HashMap<> (), new HashMap<> (), new HashMap<> ());
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

	public static UpdateState fromMap(Map<String, Object> map) {
		return objectMapper.convertValue(map, UpdateState.class);
	}
}
