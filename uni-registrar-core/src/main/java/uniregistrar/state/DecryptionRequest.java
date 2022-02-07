package uniregistrar.state;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

@JsonPropertyOrder({ "kid", "alg", "payload", "encryptedPayload" })
@JsonIgnoreProperties(ignoreUnknown=true)
public class DecryptionRequest extends JsonObject {

    @JsonProperty
    private String kid;

    @JsonProperty
    private String alg;

    @JsonProperty
    private Map<String, Object> payload;

    @JsonProperty
    private String encryptedPayload;

    private DecryptionRequest(String kid, String alg, Map<String, Object> payload, String encryptedPayload) {
        super();
        this.kid = kid;
        this.alg = alg;
        this.payload = payload;
        this.encryptedPayload = encryptedPayload;
    }

    /*
     * Factory methods
     */

    @JsonCreator
    public static DecryptionRequest build(@JsonProperty(value="kid", required=false) String kid, @JsonProperty(value="alg", required=true) String alg, @JsonProperty(value="payload", required=false) Map<String, Object> payload, @JsonProperty(value="encryptedPayload", required=false) String encryptedPayload) {
        return new DecryptionRequest(kid, alg, payload, encryptedPayload);
    }

    public static DecryptionRequest build() {
        return new DecryptionRequest(null, null, null, null);
    }

    /*
     * Serialization
     */

    public static DecryptionRequest fromJson(String json) throws JsonParseException, JsonMappingException, IOException {
        return objectMapper.readValue(json, DecryptionRequest.class);
    }

    public static DecryptionRequest fromJson(Reader reader) throws JsonParseException, JsonMappingException, IOException {
        return objectMapper.readValue(reader, DecryptionRequest.class);
    }

    public static DecryptionRequest fromMap(Map<String, Object> map) {
        return objectMapper.convertValue(map, DecryptionRequest.class);
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
    public String getEncryptedPayload() {
        return encryptedPayload;
    }

    @JsonSetter
    public void setEncryptedPayload(String encryptedPayload) {
        this.encryptedPayload = encryptedPayload;
    }
}
