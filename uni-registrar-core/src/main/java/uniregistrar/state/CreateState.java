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
public class CreateState extends State {

	private CreateState(String jobId, Map<String, Object> didState, Map<String, Object> didRegistrationMetadata, Map<String, Object> didDocumentMetadata) {
		super(jobId, didState, didRegistrationMetadata, didDocumentMetadata);
	}

	private CreateState() {
		this(null, new HashMap<> (), new HashMap<> (), new HashMap<> ());
	}

	/*
	 * Factory methods
	 */

	@JsonCreator
	public static CreateState build(@JsonProperty(value="jobId") String jobId, @JsonProperty(value="didState", required=true) Map<String, Object> didState, @JsonProperty(value="didRegistrationMetadata") Map<String, Object> didRegistrationMetadata, @JsonProperty(value="didDocumentMetadata") Map<String, Object> didDocumentMetadata) {
		return new CreateState(jobId, didState, didRegistrationMetadata, didDocumentMetadata);
	}

	public static CreateState build() {
		return new CreateState();
	}

	/*
	 * Serialization
	 */

	public static CreateState fromJson(String json) throws JsonParseException, JsonMappingException, IOException {
		return objectMapper.readValue(json, CreateState.class);
	}

	public static CreateState fromJson(Reader reader) throws JsonParseException, JsonMappingException, IOException {
		return objectMapper.readValue(reader, CreateState.class);
	}

	public static CreateState fromMap(Map<String, Object> map) {
		return objectMapper.convertValue(map, CreateState.class);
	}
}
