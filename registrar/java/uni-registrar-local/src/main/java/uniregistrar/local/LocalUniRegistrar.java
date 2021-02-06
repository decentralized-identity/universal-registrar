package uniregistrar.local;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import uniregistrar.RegistrationException;
import uniregistrar.UniRegistrar;
import uniregistrar.driver.Driver;
import uniregistrar.driver.http.HttpDriver;
import uniregistrar.local.extensions.Extension;
import uniregistrar.local.extensions.ExtensionStatus;
import uniregistrar.request.DeactivateRequest;
import uniregistrar.request.CreateRequest;
import uniregistrar.request.UpdateRequest;
import uniregistrar.state.DeactivateState;
import uniregistrar.state.CreateState;
import uniregistrar.state.UpdateState;

public class LocalUniRegistrar implements UniRegistrar {

	private static Logger log = LoggerFactory.getLogger(LocalUniRegistrar.class);

	private Map<String, Driver> drivers;
	private List<Extension> extensions = new ArrayList<Extension> ();

	public LocalUniRegistrar() {

	}

	public static LocalUniRegistrar fromConfigFile(String filePath) throws FileNotFoundException, IOException {

		final Gson gson = new Gson();

		Map<String, Driver> drivers = new HashMap<String, Driver> ();

		try (Reader reader = new FileReader(new File(filePath))) {

			JsonObject jsonObjectRoot  = gson.fromJson(reader, JsonObject.class);
			JsonArray jsonArrayDrivers = jsonObjectRoot.getAsJsonArray("drivers");

			int i = 0;

			for (Iterator<JsonElement> jsonElementsDrivers = jsonArrayDrivers.iterator(); jsonElementsDrivers.hasNext(); ) {

				i++;

				JsonObject jsonObjectDriver = (JsonObject) jsonElementsDrivers.next();

				String id = jsonObjectDriver.has("id") ? jsonObjectDriver.get("id").getAsString() : null;
				String image = jsonObjectDriver.has("image") ? jsonObjectDriver.get("image").getAsString() : null;
				String imagePort = jsonObjectDriver.has("imagePort") ? jsonObjectDriver.get("imagePort").getAsString() : null;
				String imageProperties = jsonObjectDriver.has("imageProperties") ? jsonObjectDriver.get("imageProperties").getAsString() : null;
				String url = jsonObjectDriver.has("url") ? jsonObjectDriver.get("url").getAsString() : null;

				if (image == null && url == null) throw new IllegalArgumentException("Missing 'image' and 'url' entry in driver configuration (need either one).");

				HttpDriver driver = new HttpDriver();

				if (url != null) {

					driver.setCreateUri(url + "1.0/create");
					driver.setUpdateUri(url + "1.0/update");
					driver.setDeactivateUri(url + "1.0/deactivate");
				} else {

					String httpDriverUri = image.substring(image.indexOf("/") + 1);
					if (httpDriverUri.contains(":")) httpDriverUri = httpDriverUri.substring(0, httpDriverUri.indexOf(":"));
					httpDriverUri = "http://" + httpDriverUri + ":" + (imagePort != null ? imagePort : "9080" ) + "/";

					driver.setCreateUri(httpDriverUri + "1.0/create");
					driver.setUpdateUri(httpDriverUri + "1.0/update");
					driver.setDeactivateUri(httpDriverUri + "1.0/deactivate");

					if ("true".equals(imageProperties)) {

						driver.setPropertiesUri(httpDriverUri + "1.0/properties");
					}
				}

				if (id == null) {

					id = "driver";
					if (image != null) id += "-" + image;
					if (image == null || drivers.containsKey(id)) id += "-" + Integer.toString(i);
				}

				drivers.put(id, driver);

				if (log.isInfoEnabled()) log.info("Added driver '" + id + "' at " + driver.getCreateUri() + " and " + driver.getUpdateUri() + " and " + driver.getDeactivateUri() + " (" + driver.getPropertiesUri() + ")");
			}
		}

		LocalUniRegistrar localUniRegistrar = new LocalUniRegistrar();
		localUniRegistrar.setDrivers(drivers);

		return localUniRegistrar;
	}

	@Override
	public CreateState create(String driverId, CreateRequest createRequest) throws RegistrationException {

		if (driverId == null) throw new NullPointerException();
		if (createRequest == null) throw new NullPointerException();

		if (this.getDrivers() == null) throw new RegistrationException("No drivers configured.");

		// start time

		long start = System.currentTimeMillis();

		// prepare create state

		CreateState createState = CreateState.build();
		ExtensionStatus extensionStatus = new ExtensionStatus();

		// execute extensions (before)

		if (! extensionStatus.skipExtensionsBefore()) {

			for (Extension extension : this.getExtensions()) {

				extensionStatus.or(extension.beforeCreate(driverId, createRequest, createState, this));
				if (extensionStatus.skipExtensionsBefore()) break;
			}
		}

		// select and execute driver

		if (! extensionStatus.skipDriver()) {

			Driver driver = this.getDrivers().get(driverId);
			if (driver == null) throw new RegistrationException("Unknown driver: " + driverId);
			if (log.isDebugEnabled()) log.debug("Attemping to create " + createRequest + " with driver " + driver.getClass());

			CreateState driverCreateState = driver.create(createRequest);

			if (driverCreateState != null) {

				createState.setJobId(driverCreateState.getJobId());
				createState.setDidState(driverCreateState.getDidState());
				createState.setMethodMetadata(driverCreateState.getMethodMetadata());
			}

			createState.getRegistrarMetadata().put("driverId", driverId);
		}

		// execute extensions (after)

		if (! extensionStatus.skipExtensionsAfter()) {

			for (Extension extension : this.getExtensions()) {

				extensionStatus.or(extension.afterCreate(driverId, createRequest, createState, this));
				if (extensionStatus.skipExtensionsAfter()) break;
			}
		}

		// stop time

		long stop = System.currentTimeMillis();

		createState.getRegistrarMetadata().put("duration", Long.valueOf(stop - start));

		// done

		return createState;
	}

	@Override
	public UpdateState update(String driverId, UpdateRequest updateRequest) throws RegistrationException {

		if (driverId == null) throw new NullPointerException();
		if (updateRequest == null) throw new NullPointerException();

		if (this.getDrivers() == null) throw new RegistrationException("No drivers configured.");

		// start time

		long start = System.currentTimeMillis();

		// prepare update state

		UpdateState updateState = UpdateState.build();
		ExtensionStatus extensionStatus = new ExtensionStatus();

		// execute extensions (before)

		if (! extensionStatus.skipExtensionsBefore()) {

			for (Extension extension : this.getExtensions()) {

				extensionStatus.or(extension.beforeUpdate(driverId, updateRequest, updateState, this));
				if (extensionStatus.skipExtensionsBefore()) break;
			}
		}

		// select and execute driver

		if (! extensionStatus.skipDriver()) {

			Driver driver = this.getDrivers().get(driverId);
			if (driver == null) throw new RegistrationException("Unknown driver: " + driverId);
			if (log.isDebugEnabled()) log.debug("Attemping to update " + updateRequest + " with driver " + driver.getClass());

			UpdateState driverUpdateState = driver.update(updateRequest);
			updateState.setJobId(driverUpdateState.getJobId());
			updateState.setDidState(driverUpdateState.getDidState());
			updateState.setMethodMetadata(driverUpdateState.getMethodMetadata());

			updateState.getRegistrarMetadata().put("driverId", driverId);
		}

		// execute extensions (after)

		if (! extensionStatus.skipExtensionsAfter()) {

			for (Extension extension : this.getExtensions()) {

				extensionStatus.or(extension.afterUpdate(driverId, updateRequest, updateState, this));
				if (extensionStatus.skipExtensionsAfter()) break;
			}
		}

		// stop time

		long stop = System.currentTimeMillis();

		updateState.getRegistrarMetadata().put("duration", Long.valueOf(stop - start));

		// done

		return updateState;
	}

	@Override
	public DeactivateState deactivate(String driverId, DeactivateRequest deactivateRequest) throws RegistrationException {

		if (driverId == null) throw new NullPointerException();
		if (deactivateRequest == null) throw new NullPointerException();

		if (this.getDrivers() == null) throw new RegistrationException("No drivers configured.");

		// start time

		long start = System.currentTimeMillis();

		// prepare deactivate state

		DeactivateState deactivateState = DeactivateState.build();
		ExtensionStatus extensionStatus = new ExtensionStatus();

		// execute extensions (before)

		if (! extensionStatus.skipExtensionsBefore()) {

			for (Extension extension : this.getExtensions()) {

				extensionStatus.or(extension.beforeDeactivate(driverId, deactivateRequest, deactivateState, this));
				if (extensionStatus.skipExtensionsBefore()) break;
			}
		}

		// select and execute driver

		if (! extensionStatus.skipDriver()) {

			Driver driver = this.getDrivers().get(driverId);
			if (driver == null) throw new RegistrationException("Unknown driver: " + driverId);
			if (log.isDebugEnabled()) log.debug("Attemping to deactivate " + deactivateRequest + " with driver " + driver.getClass());

			DeactivateState driverDeactivateState = driver.deactivate(deactivateRequest);
			deactivateState.setJobId(driverDeactivateState.getJobId());
			deactivateState.setDidState(driverDeactivateState.getDidState());
			deactivateState.setMethodMetadata(driverDeactivateState.getMethodMetadata());

			deactivateState.getRegistrarMetadata().put("driverId", driverId);
		}

		// execute extensions (after)

		if (! extensionStatus.skipExtensionsAfter()) {

			for (Extension extension : this.getExtensions()) {

				extensionStatus.or(extension.afterDeactivate(driverId, deactivateRequest, deactivateState, this));
				if (extensionStatus.skipExtensionsAfter()) break;
			}
		}

		// stop time

		long stop = System.currentTimeMillis();

		deactivateState.getRegistrarMetadata().put("duration", Long.valueOf(stop - start));

		// done

		return deactivateState;
	}

	@Override
	public Map<String, Map<String, Object>> properties() throws RegistrationException {

		if (this.getDrivers() == null) throw new RegistrationException("No drivers configured.");

		Map<String, Map<String, Object>> properties = new HashMap<String, Map<String, Object>> ();

		for (Entry<String, Driver> driver : this.getDrivers().entrySet()) {

			if (log.isDebugEnabled()) log.debug("Loading properties for driver " + driver.getKey() + " (" + driver.getValue().getClass().getSimpleName() + ")");

			Map<String, Object> driverProperties = driver.getValue().properties();
			if (driverProperties == null) driverProperties = Collections.emptyMap();

			properties.put(driver.getKey(), driverProperties);
		}

		if (log.isDebugEnabled()) log.debug("Loading properties: " + properties);

		return properties;
	}

	/*
	 * Getters and setters
	 */

	public Map<String, Driver> getDrivers() {

		return this.drivers;
	}

	@SuppressWarnings("unchecked")
	public <T extends Driver> T getDriver(Class<T> driverClass) {

		for (Driver driver : this.getDrivers().values()) {

			if (driverClass.isAssignableFrom(driver.getClass())) return (T) driver;
		}

		return null;
	}

	public void setDrivers(Map<String, Driver> drivers) {

		this.drivers = drivers;
	}

	public List<Extension> getExtensions() {

		return this.extensions;
	}

	public void setExtensions(List<Extension> extensions) {

		this.extensions = extensions;
	}
}
