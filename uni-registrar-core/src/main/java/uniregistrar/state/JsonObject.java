package uniregistrar.state;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.DIDDocument;
import jakarta.json.Json;

import java.util.Map;

public class JsonObject {

    public static final String MEDIA_TYPE = "application/json";

    protected static final ObjectMapper objectMapper = new ObjectMapper();

    protected JsonObject() {
    }

    /*
     * Serialization
     */

    public Map<String, Object> toMap() {
        return objectMapper.convertValue(this, Map.class);
    }

    public String toJson() throws JsonProcessingException {
        return objectMapper.writeValueAsString(this);
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
}
