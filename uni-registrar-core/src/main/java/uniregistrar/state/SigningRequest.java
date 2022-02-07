package uniregistrar.state;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.DIDDocument;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

@JsonPropertyOrder({ "kid", "alg", "payload", "serializedPayload" })
@JsonIgnoreProperties(ignoreUnknown=true)
public class SigningRequest extends JsonObject {

    @JsonProperty
    private String kid;

    @JsonProperty
    private String alg;

    @JsonProperty
    private Map<String, Object> payload;

    @JsonProperty
    private String serializedPayload;

    private SigningRequest(String kid, String alg, Map<String, Object> payload, String serializedPayload) {
        super();
        this.kid = kid;
        this.alg = alg;
        this.payload = payload;
        this.serializedPayload = serializedPayload;
    }

    /*
     * Factory methods
     */

    @JsonCreator
    public static SigningRequest build(@JsonProperty(value="kid", required=false) String kid, @JsonProperty(value="alg", required=true) String alg, @JsonProperty(value="payload", required=false) Map<String, Object> payload, @JsonProperty(value="serializedPayload", required=false) String serializedPayload) {
        return new SigningRequest(kid, alg, payload, serializedPayload);
    }

    public static SigningRequest build() {
        return new SigningRequest(null, null, null, null);
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
