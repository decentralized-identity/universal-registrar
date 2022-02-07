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
public class SigningResponse extends JsonObject {

    @JsonProperty
    private String signature;

    private SigningResponse(String signature) {
        super();
        this.signature = signature;
    }

    /*
     * Factory methods
     */

    @JsonCreator
    public static SigningResponse build(@JsonProperty(value="signature", required=false) String signature) {
        return new SigningResponse(signature);
    }

    public static SigningResponse build() {
        return new SigningResponse(null);
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
    public String getSignature() {
        return signature;
    }

    @JsonSetter
    public void setSignature(String signature) {
        this.signature = signature;
    }
}
