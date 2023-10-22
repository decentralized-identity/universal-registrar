package uniregistrar.local.configuration;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniregistrar.driver.Driver;
import uniregistrar.driver.http.HttpDriver;
import uniregistrar.local.LocalUniRegistrar;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;

public class LocalUniRegistrarConfigurator {

    private static final Logger log = LoggerFactory.getLogger(LocalUniRegistrarConfigurator.class);

    public static void configureLocalUniRegistrar(String filePath, LocalUniRegistrar localUniRegistrar) throws IOException {

        final Gson gson = new Gson();

        Map<String, Driver> drivers = new LinkedHashMap<>();

        try (Reader reader = new FileReader(filePath)) {

            JsonObject jsonObjectRoot  = gson.fromJson(reader, JsonObject.class);
            JsonArray jsonArrayDrivers = jsonObjectRoot.getAsJsonArray("drivers");

            for (JsonElement jsonArrayDriver : jsonArrayDrivers) {

                JsonObject jsonObjectDriver = (JsonObject) jsonArrayDriver;

                String method = jsonObjectDriver.has("method") ? jsonObjectDriver.get("method").getAsString() : null;
                String url = jsonObjectDriver.has("url") ? jsonObjectDriver.get("url").getAsString() : null;
                String propertiesEndpoint = jsonObjectDriver.has("propertiesEndpoint") ? jsonObjectDriver.get("propertiesEndpoint").getAsString() : null;

                if (method == null) throw new IllegalArgumentException("Missing 'method' entry in driver configuration.");
                if (url == null) throw new IllegalArgumentException("Missing 'url' entry in driver configuration.");

                // construct HTTP driver

                HttpDriver driver = new HttpDriver();

                if (! url.endsWith("/")) url = url + "/";

                driver.setCreateUri(url + "1.0/create");
                driver.setUpdateUri(url + "1.0/update");
                driver.setDeactivateUri(url + "1.0/deactivate");
                if ("true".equals(propertiesEndpoint)) driver.setPropertiesUri(url + "1.0/properties");

                // done

                drivers.put(method, driver);
                if (log.isInfoEnabled()) log.info("Added driver for method '" + method + "' at " + driver.getCreateUri() + " and " + driver.getUpdateUri() + " and " + driver.getDeactivateUri() + " (" + driver.getPropertiesUri() + ")");
            }
        }

        localUniRegistrar.setDrivers(drivers);
    }
}
