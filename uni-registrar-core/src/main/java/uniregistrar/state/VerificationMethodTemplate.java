package uniregistrar.state;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import uniregistrar.JsonObject;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;

@JsonPropertyOrder({ "id", "type", "controller", "publicKeyJwk", "purpose" })
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerificationMethodTemplate extends JsonObject {

    @JsonProperty
    private String id;

    @JsonProperty
    private String type;

    @JsonProperty
    private String controller;

    @JsonProperty
    private Map<String, Object> publicKeyJwk;

    @JsonProperty
    private List<String> purpose;

    private VerificationMethodTemplate(String id, String type, String controller, Map<String, Object> publicKeyJwk, List<String> purpose) {
        super();
        this.id = id;
        this.type = type;
        this.controller = controller;
        this.publicKeyJwk = publicKeyJwk;
        this.purpose = purpose;
    }

    private VerificationMethodTemplate() {
        super();
    }

    /*
     * Factory methods
     */

    @JsonCreator
    public static VerificationMethodTemplate build(@JsonProperty(value="id") String id, @JsonProperty(value="type") String type, @JsonProperty(value="controller") String controller, @JsonProperty(value="publicKeyJwk") Map<String, Object> publicKeyJwk, @JsonProperty(value="purpose") List<String> purpose) {
        return new VerificationMethodTemplate(id, type, controller, publicKeyJwk, purpose);
    }

    public static VerificationMethodTemplate build() {
        return new VerificationMethodTemplate();
    }

    /*
     * Serialization
     */

    public static VerificationMethodTemplate fromJson(String json) throws JsonParseException, JsonMappingException, IOException {
        return objectMapper.readValue(json, VerificationMethodTemplate.class);
    }

    public static VerificationMethodTemplate fromJson(Reader reader) throws JsonParseException, JsonMappingException, IOException {
        return objectMapper.readValue(reader, VerificationMethodTemplate.class);
    }

    public static VerificationMethodTemplate fromMap(Map<String, Object> map) {
        return objectMapper.convertValue(map, VerificationMethodTemplate.class);
    }

    /*
     * Getters and setters
     */

    @JsonGetter
    public String getId() {
        return id;
    }

    @JsonSetter
    public void setId(String id) {
        this.id = id;
    }

    @JsonGetter
    public String getType() {
        return type;
    }

    @JsonSetter
    public void setType(String type) {
        this.type = type;
    }

    @JsonGetter
    public String getController() {
        return controller;
    }

    @JsonSetter
    public void setController(String controller) {
        this.controller = controller;
    }

    @JsonGetter
    public Map<String, Object> getPublicKeyJwk() {
        return publicKeyJwk;
    }

    @JsonSetter
    public void setPublicKeyJwk(Map<String, Object> publicKeyJwk) {
        this.publicKeyJwk = publicKeyJwk;
    }

    @JsonGetter
    public List<String> getPurpose() {
        return purpose;
    }

    @JsonSetter
    public void setPurpose(List<String> purpose) {
        this.purpose = purpose;
    }
}
