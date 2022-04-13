package uniregistrar.state;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import uniregistrar.JsonObject;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;

@JsonPropertyOrder({ "id", "type", "controller", "purpose" })
@JsonIgnoreProperties(ignoreUnknown=true)
public class VerificationMethodTemplate extends JsonObject {

    @JsonProperty
    private String id;

    @JsonProperty
    private String type;

    @JsonProperty
    private String controller;

    @JsonProperty
    private List<String> purpose;

    private VerificationMethodTemplate(String id, String type, String controller, List<String> purpose) {
        super();
        this.id = id;
        this.type = type;
        this.controller = controller;
        this.purpose = purpose;
    }

    /*
     * Factory methods
     */

    @JsonCreator
    public static VerificationMethodTemplate build(@JsonProperty(value="id", required=false) String id, @JsonProperty(value="type", required=false) String type, @JsonProperty(value="controller", required=false) String controller, @JsonProperty(value="purpose", required=false) List<String> purpose) {
        return new VerificationMethodTemplate(id, type, controller, purpose);
    }

    public static VerificationMethodTemplate build() {
        return new VerificationMethodTemplate(null, null, null, null);
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getController() {
        return controller;
    }

    public void setController(String controller) {
        this.controller = controller;
    }

    public List<String> getPurpose() {
        return purpose;
    }

    public void setPurpose(List<String> purpose) {
        this.purpose = purpose;
    }
}
