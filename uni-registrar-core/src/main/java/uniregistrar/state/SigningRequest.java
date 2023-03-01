package uniregistrar.state;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import uniregistrar.JsonObject;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

@JsonPropertyOrder({ "kid", "alg", "purpose", "payload", "serializedPayload" })
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SigningRequest extends JsonObject {

    @JsonProperty
    private String kid;

    @JsonProperty
    private String alg;

    @JsonProperty
    private String purpose;

    @JsonProperty
    private Map<String, Object> payload;

    @JsonProperty
    private String serializedPayload;

    private SigningRequest(String kid, String alg, String purpose, Map<String, Object> payload, String serializedPayload) {
        super();
        this.kid = kid;
        this.alg = alg;
        this.purpose = purpose;
        this.payload = payload;
        this.serializedPayload = serializedPayload;
    }

    private SigningRequest() {
        this(null, null, null, null, null);
    }

    /*
     * Factory methods
     */

    @JsonCreator
    public static SigningRequest build(@JsonProperty(value="kid", required=false) String kid, @JsonProperty(value="alg", required=true) String alg, @JsonProperty(value="purpose", required=false) String purpose, @JsonProperty(value="payload", required=false) Map<String, Object> payload, @JsonProperty(value="serializedPayload", required=false) String serializedPayload) {
        return new SigningRequest(kid, alg, purpose, payload, serializedPayload);
    }

    public static SigningRequest build() {
        return new SigningRequest();
    }

    /*
     * Serialization
     */

    public static SigningRequest fromJson(String json) throws JsonParseException, JsonMappingException, IOException {
        return objectMapper.readValue(json, SigningRequest.class);
    }

    public static SigningRequest fromJson(Reader reader) throws JsonParseException, JsonMappingException, IOException {
        return objectMapper.readValue(reader, SigningRequest.class);
    }

    public static SigningRequest fromMap(Map<String, Object> map) {
        return objectMapper.convertValue(map, SigningRequest.class);
    }

    /*
     * Getters and setters
     */

    @JsonGetter
    public String getKid() {
        return kid;
    }

    @JsonSetter
    public void setKid(String kid) {
        this.kid = kid;
    }

    @JsonGetter
    public String getAlg() {
        return alg;
    }

    @JsonSetter
    public void setAlg(String alg) {
        this.alg = alg;
    }

    @JsonGetter
    public String getPurpose() {
        return purpose;
    }

    @JsonSetter
    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    @JsonGetter
    public Map<String, Object> getPayload() {
        return payload;
    }

    @JsonSetter
    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    @JsonGetter
    public String getSerializedPayload() {
        return serializedPayload;
    }

    @JsonSetter
    public void setSerializedPayload(String serializedPayload) {
        this.serializedPayload = serializedPayload;
    }
}
