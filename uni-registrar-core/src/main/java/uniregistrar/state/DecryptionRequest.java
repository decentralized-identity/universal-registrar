package uniregistrar.state;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import uniregistrar.JsonObject;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

@JsonPropertyOrder({ "kid", "enc", "purpose", "payload", "encryptedPayload" })
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DecryptionRequest extends JsonObject {

    @JsonProperty
    private String kid;

    @JsonProperty
    private String enc;

    @JsonProperty
    private String purpose;

    @JsonProperty
    private Map<String, Object> payload;

    @JsonProperty
    private String encryptedPayload;

    private DecryptionRequest(String kid, String enc, String purpose, Map<String, Object> payload, String encryptedPayload) {
        super();
        this.kid = kid;
        this.enc = enc;
        this.purpose = purpose;
        this.payload = payload;
        this.encryptedPayload = encryptedPayload;
    }

    private DecryptionRequest() {
        this(null, null, null, null, null);
    }

    /*
     * Factory methods
     */

    @JsonCreator
    public static DecryptionRequest build(@JsonProperty(value="kid") String kid, @JsonProperty(value="enc", required=true) String enc, @JsonProperty(value="purpose") String purpose, @JsonProperty(value="payload") Map<String, Object> payload, @JsonProperty(value="encryptedPayload") String encryptedPayload) {
        return new DecryptionRequest(kid, enc, purpose, payload, encryptedPayload);
    }

    public static DecryptionRequest build() {
        return new DecryptionRequest();
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
    public String getEnc() {
        return enc;
    }

    @JsonSetter
    public void setEnc(String enc) {
        this.enc = enc;
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
    public String getEncryptedPayload() {
        return encryptedPayload;
    }

    @JsonSetter
    public void setEncryptedPayload(String encryptedPayload) {
        this.encryptedPayload = encryptedPayload;
    }
}
