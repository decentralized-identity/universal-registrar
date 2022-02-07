package uniregistrar.state;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonObject {

    public static final String MEDIA_TYPE = "application/json";

    protected static final ObjectMapper objectMapper = new ObjectMapper();

    protected JsonObject() {
    }

    /*
     * Serialization
     */

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
