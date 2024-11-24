package uniregistrar.local.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniregistrar.driver.Driver;
import uniregistrar.driver.http.HttpDriver;
import uniregistrar.local.LocalUniRegistrar;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LocalUniRegistrarConfigurator {

    private static final Logger log = LoggerFactory.getLogger(LocalUniRegistrarConfigurator.class);

    private final static ObjectMapper objectMapper = new ObjectMapper();

    public static void configureLocalUniRegistrar(String filePath, LocalUniRegistrar localUniRegistrar) throws IOException {

        Map<String, Driver> drivers = new LinkedHashMap<>();

        try (Reader reader = new FileReader(filePath)) {

            Map<String, Object> jsonRoot = objectMapper.readValue(reader, Map.class);
            List<Map<String, Object>> jsonDrivers = (List<Map<String, Object>>) jsonRoot.get("drivers");

            for (Map<String, Object> jsonDriver : jsonDrivers) {

                String method = jsonDriver.containsKey("method") ? (String) jsonDriver.get("method") : null;
                String url = jsonDriver.containsKey("url") ? (String) jsonDriver.get("url") : null;
                String propertiesEndpoint = jsonDriver.containsKey("propertiesEndpoint") ? (String) jsonDriver.get("propertiesEndpoint") : null;
                String includeMethodParameter = jsonDriver.containsKey("includeMethodParameter") ? (String) jsonDriver.get("includeMethodParameter") : null;
                Map<String, Object> traits = jsonDriver.containsKey("traits") ? (Map<String, Object>) jsonDriver.get("traits") : null;

                if (method == null) throw new IllegalArgumentException("Missing 'method' entry in driver configuration.");
                if (url == null) throw new IllegalArgumentException("Missing 'url' entry in driver configuration.");

                // construct HTTP driver

                HttpDriver driver = new HttpDriver();
                driver.setMethod(method);

                if (! url.endsWith("/")) url = url + "/";
                driver.setCreateUri(url + "1.0/create");
                driver.setUpdateUri(url + "1.0/update");
                driver.setDeactivateUri(url + "1.0/deactivate");
                driver.setExecuteUri(url + "1.0/execute");
                driver.setCreateResourceUri(url + "1.0/createResource");
                driver.setUpdateResourceUri(url + "1.0/updateResource");
                driver.setDeactivateResourceUri(url + "1.0/deactivateResource");
                if ("true".equals(propertiesEndpoint)) driver.setPropertiesUri(url + "1.0/properties");
                if ("true".equals(includeMethodParameter)) driver.setIncludeMethodParameter(Boolean.valueOf(includeMethodParameter));
                if (traits != null) driver.setTraits(traits);

                // done

                drivers.put(method, driver);
                if (log.isInfoEnabled()) log.info("Added driver for method '" + method + "' at " + driver.getCreateUri() + " and " + driver.getUpdateUri() + " and " + driver.getDeactivateUri() + " and " + driver.getExecuteUri() + " (" + driver.getPropertiesUri() + ")" + " (" + driver.getIncludeMethodParameter() + ")");
            }
        }

        localUniRegistrar.setDrivers(drivers);
    }
}
