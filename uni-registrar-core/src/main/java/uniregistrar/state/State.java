package uniregistrar.state;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class State {

    public static final String MEDIA_TYPE = "application/json";

    protected static final ObjectMapper objectMapper = new ObjectMapper();

    @JsonProperty
    private String jobId;

    @JsonProperty
    private Map<String, Object> didState;

    @JsonProperty
    private Map<String, Object> didRegistrationMetadata;

    @JsonProperty
    private Map<String, Object> didDocumentMetadata;

    protected State(String jobId, Map<String, Object> didState, Map<String, Object> didRegistrationMetadata, Map<String, Object> didDocumentMetadata) {
        this.jobId = jobId;
        this.didState = didState;
        this.didRegistrationMetadata = didRegistrationMetadata;
        this.didDocumentMetadata = didDocumentMetadata;
    }

    /*
     * Factory methods
     */

    @JsonCreator
    public static State build(@JsonProperty(value="jobId", required=false) String jobId, @JsonProperty(value="didState", required=true) Map<String, Object> didState, @JsonProperty(value="didRegistrationMetadata", required=false) Map<String, Object> didRegistrationMetadata, @JsonProperty(value="didDocumentMetadata", required=false) Map<String, Object> didDocumentMetadata) {
        return new State(jobId, didState, didRegistrationMetadata, didDocumentMetadata);
    }

    public static State build() {
        return new State(null, new HashMap<>(), new HashMap<>(), new HashMap<>());
    }

    /*
     * Serialization
     */

    public static State fromJson(String json) throws JsonParseException, JsonMappingException, IOException {
        return objectMapper.readValue(json, State.class);
    }

    public static State fromJson(Reader reader) throws JsonParseException, JsonMappingException, IOException {
        return objectMapper.readValue(reader, State.class);
    }

    public String toJson() throws JsonProcessingException {
        return objectMapper.writeValueAsString(this);
    }

    /*
     * Helper methods
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
     * Object methods
     */

    public String toString() {
        try {
            return this.toJson();
        } catch (JsonProcessingException ex) {
            return ex.getMessage();
        }
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
}
