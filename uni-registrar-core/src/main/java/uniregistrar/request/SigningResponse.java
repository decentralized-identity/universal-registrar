package uniregistrar.request;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import uniregistrar.JsonObject;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

@JsonPropertyOrder({ "signature" })
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SigningResponse extends JsonObject {

    @JsonProperty
    private String kid;

    @JsonProperty
    private String alg;

    @JsonProperty
    private String purpose;

    @JsonProperty
    private String signature;

    private SigningResponse(String kid, String alg, String purpose, String signature) {
        super();
        this.kid = kid;
        this.alg = alg;
        this.purpose = purpose;
        this.signature = signature;
    }

    private SigningResponse() {
        this(null, null, null, null);
    }

    /*
     * Factory methods
     */

    @JsonCreator
    public static SigningResponse build(@JsonProperty(value="kid") String kid, @JsonProperty(value="alg") String alg, @JsonProperty(value="purpose") String purpose, @JsonProperty(value="signature") String signature) {
        return new SigningResponse(kid, alg, purpose, signature);
    }

    public static SigningResponse build() {
        return new SigningResponse();
    }

    /*
     * Serialization
     */

    public static SigningResponse fromJson(String json) throws JsonParseException, JsonMappingException, IOException {
        return objectMapper.readValue(json, SigningResponse.class);
    }

    public static SigningResponse fromJson(Reader reader) throws JsonParseException, JsonMappingException, IOException {
        return objectMapper.readValue(reader, SigningResponse.class);
    }

    public static SigningResponse fromMap(Map<String, Object> map) {
        return objectMapper.convertValue(map, SigningResponse.class);
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
    public String getSignature() {
        return signature;
    }

    @JsonSetter
    public void setSignature(String signature) {
        this.signature = signature;
    }
}
