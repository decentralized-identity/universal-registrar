package uniregistrar.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniregistrar.openapi.RFC3339DateFormat;
import uniregistrar.openapi.model.RegistrarRequest;
import uniregistrar.openapi.model.RegistrarState;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

public class HttpBindingUtil {

    private static final Logger log = LoggerFactory.getLogger(HttpBindingUtil.class);

    private static final ObjectMapper objectMapper = JsonMapper.builder()
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
            .enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
            .defaultDateFormat(new RFC3339DateFormat())
            .addModule(new JavaTimeModule())
            .build();

    public static <T extends RegistrarRequest> T fromMapRequest(Map<String, Object> map, Class<T> cl) {
        return objectMapper.convertValue(map, cl);
    }

    public static Map<String, Object> toMapRequest(RegistrarRequest request) {
        return objectMapper.convertValue(request, Map.class);
    }

    public static <T extends RegistrarState> T fromMapState(Map<String, Object> map, Class<T> cl) {
        return objectMapper.convertValue(map, cl);
    }

    public static Map<String, Object> toMapState(RegistrarState state) {
        return objectMapper.convertValue(state, Map.class);
    }

    public static String toHttpBodyMap(Map<String, Object> map) throws JsonProcessingException {
        return objectMapper.writeValueAsString(map);
    }

    public static Map<String, Object> fromHttpBodyMap(String httpBody) throws JsonProcessingException {
        return objectMapper.readValue(httpBody, Map.class);
    }

    public static Map<String, Object> fromHttpBodyMap(Reader httpBody) throws IOException {
        return objectMapper.readValue(httpBody, Map.class);
    }

    public static <T extends RegistrarRequest> T fromHttpBodyRequest(String httpBody, Class<T> cl) throws JsonProcessingException {
        return objectMapper.readValue(httpBody, cl);
    }

    public static <T extends RegistrarRequest> T fromHttpBodyRequest(Reader httpBody, Class<T> cl) throws IOException {
        return objectMapper.readValue(httpBody, cl);
    }

    public static <T extends RegistrarState> T fromHttpBodyState(String httpBody, Class<T> cl) throws JsonProcessingException {
        return objectMapper.readValue(httpBody, cl);
    }

    public static <T extends RegistrarState> T fromHttpBodyState(Reader httpBody, Class<T> cl) throws IOException {
        return objectMapper.readValue(httpBody, cl);
    }

    public static String toHttpBodyRequest(RegistrarRequest registrarRequest) {
        try {
            return objectMapper.writeValueAsString(registrarRequest);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public static String toHttpBodyState(RegistrarState registrarState) {
        try {
            return objectMapper.writeValueAsString(registrarState);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
}
