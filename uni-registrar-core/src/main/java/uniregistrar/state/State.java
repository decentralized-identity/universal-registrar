package uniregistrar.state;

import com.fasterxml.jackson.annotation.*;

import java.util.Map;

@JsonPropertyOrder({ "jobId", "didState", "didRegistrationMetadata", "didDocumentMetadata" })
@JsonIgnoreProperties(ignoreUnknown=true)
public class State extends JsonObject {

    @JsonProperty
    private String jobId;

    @JsonProperty
    private Map<String, Object> didState;

    @JsonProperty
    private Map<String, Object> didRegistrationMetadata;

    @JsonProperty
    private Map<String, Object> didDocumentMetadata;

    protected State(String jobId, Map<String, Object> didState, Map<String, Object> didRegistrationMetadata, Map<String, Object> didDocumentMetadata) {
        super();
        this.jobId = jobId;
        this.didState = didState;
        this.didRegistrationMetadata = didRegistrationMetadata;
        this.didDocumentMetadata = didDocumentMetadata;
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
