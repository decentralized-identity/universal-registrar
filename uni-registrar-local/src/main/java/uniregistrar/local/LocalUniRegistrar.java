package uniregistrar.local;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniregistrar.RegistrationException;
import uniregistrar.UniRegistrar;
import uniregistrar.driver.Driver;
import uniregistrar.driver.http.HttpDriver;
import uniregistrar.local.extensions.Extension;
import uniregistrar.local.extensions.ExtensionStatus;
import uniregistrar.request.CreateRequest;
import uniregistrar.request.DeactivateRequest;
import uniregistrar.request.UpdateRequest;
import uniregistrar.state.CreateState;
import uniregistrar.state.DeactivateState;
import uniregistrar.state.UpdateState;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class LocalUniRegistrar implements UniRegistrar {

	private static Logger log = LoggerFactory.getLogger(LocalUniRegistrar.class);

	private Map<String, Driver> drivers = new LinkedHashMap<String, Driver> ();
	private List<Extension> extensions = new ArrayList<Extension> ();

	public LocalUniRegistrar() {

	}

	public static LocalUniRegistrar fromConfigFile(String filePath) throws FileNotFoundException, IOException {

		final Gson gson = new Gson();

		Map<String, Driver> drivers = new LinkedHashMap<String, Driver> ();

		try (Reader reader = new FileReader(new File(filePath))) {

			JsonObject jsonObjectRoot  = gson.fromJson(reader, JsonObject.class);
			JsonArray jsonArrayDrivers = jsonObjectRoot.getAsJsonArray("drivers");

			int i = 0;

			for (Iterator<JsonElement> jsonElementsDrivers = jsonArrayDrivers.iterator(); jsonElementsDrivers.hasNext(); ) {

				i++;

				JsonObject jsonObjectDriver = (JsonObject) jsonElementsDrivers.next();

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

		LocalUniRegistrar localUniRegistrar = new LocalUniRegistrar();
		localUniRegistrar.setDrivers(drivers);

		return localUniRegistrar;
	}

	@Override
	public CreateState create(String method, CreateRequest createRequest) throws RegistrationException {

		return this.create(method, createRequest, null);
	}

	public CreateState create(String method, CreateRequest createRequest, Map<String, Object> initialExecutionState) throws RegistrationException {

		if (method == null) throw new NullPointerException();
		if (createRequest == null) throw new NullPointerException();

		if (this.getDrivers() == null) throw new RegistrationException("No drivers configured.");

		// start time

		long start = System.currentTimeMillis();

		// prepare execution state

		Map<String, Object> executionState = new HashMap<>();
		if (initialExecutionState != null) executionState.putAll(initialExecutionState);

		// prepare create state

		CreateState createState = CreateState.build();
		ExtensionStatus extensionStatus = new ExtensionStatus();

		// [before create]

		List<Extension> skippedBeforeCreateExtensions = new ArrayList<>();
		List<Extension> inapplicableBeforeCreateExtensions = new ArrayList<>();

		for (Extension.BeforeCreateExtension extension : this.getBeforeCreateExtensions()) {
			if (extensionStatus.skipExtensionsBefore()) { skippedBeforeCreateExtensions.add(extension); continue; }
			ExtensionStatus returnedExtensionStatus = extension.beforeCreate(method, createRequest, createState, executionState, this);
			extensionStatus.or(returnedExtensionStatus);
			if (returnedExtensionStatus == null) { inapplicableBeforeCreateExtensions.add(extension); continue; }
			if (log.isDebugEnabled()) log.debug("Executed extension (beforeCreate) " + extension.getClass().getSimpleName() + " with request " + createRequest + " and state " + createState + " and execution state " + executionState);
		}

		if (log.isDebugEnabled()) log.debug("Skipped extensions (beforeCreate): {}, inapplicable extensions (beforeCreate): {}", extensionClassNames(skippedBeforeCreateExtensions), extensionClassNames(inapplicableBeforeCreateExtensions));

		// [create]

		if (! extensionStatus.skipDriver()) {

			Driver driver = this.getDrivers().get(method);
			if (driver == null) throw new RegistrationException(RegistrationException.ERROR_BADREQUEST, "Unsupported method: " + method);
			if (log.isDebugEnabled()) log.debug("Attempting to create " + createRequest + " with driver " + driver.getClass().getSimpleName());

			CreateState driverCreateState = driver.create(createRequest);

			if (driverCreateState != null) {

				createState.setJobId(driverCreateState.getJobId());
				createState.setDidState(driverCreateState.getDidState());
				createState.setDidDocumentMetadata(driverCreateState.getDidDocumentMetadata());
			}

			createState.getDidRegistrationMetadata().put("method", method);
		}

		// [after create]

		List<Extension> skippedAfterCreateExtensions = new ArrayList<>();
		List<Extension> inapplicableAfterCreateExtensions = new ArrayList<>();

		for (Extension.AfterCreateExtension extension : this.getAfterCreateExtensions()) {
			if (extensionStatus.skipExtensionsAfter()) { skippedAfterCreateExtensions.add(extension); continue; }
			ExtensionStatus returnedExtensionStatus = extension.afterCreate(method, createRequest, createState, executionState, this);
			extensionStatus.or(returnedExtensionStatus);
			if (returnedExtensionStatus == null) { inapplicableAfterCreateExtensions.add(extension); continue; }
			if (log.isDebugEnabled()) log.debug("Executed extension (afterCreate) " + extension.getClass().getSimpleName() + " with request " + createRequest + " and state " + createState + " and execution state " + executionState);
		}

		if (log.isDebugEnabled()) log.debug("Skipped extensions (afterCreate): {}, inapplicable extensions (afterCreate): {}", extensionClassNames(skippedAfterCreateExtensions), extensionClassNames(inapplicableAfterCreateExtensions));

		// additional metadata

		long stop = System.currentTimeMillis();
		createState.getDidRegistrationMetadata().put("duration", Long.valueOf(stop - start));

		// done

		return createState;
	}

	@Override
	public UpdateState update(String method, UpdateRequest updateRequest) throws RegistrationException {

		return this.update(method, updateRequest, null);
	}

	public UpdateState update(String method, UpdateRequest updateRequest, Map<String, Object> initialExecutionState) throws RegistrationException {

		if (method == null) throw new NullPointerException();
		if (updateRequest == null) throw new NullPointerException();

		if (this.getDrivers() == null) throw new RegistrationException("No drivers configured.");

		// start time

		long start = System.currentTimeMillis();

		// prepare execution state

		Map<String, Object> executionState = new HashMap<>();
		if (initialExecutionState != null) executionState.putAll(initialExecutionState);

		// prepare update state

		UpdateState updateState = UpdateState.build();
		ExtensionStatus extensionStatus = new ExtensionStatus();

		// [before update]

		List<Extension> skippedBeforeUpdateExtensions = new ArrayList<>();
		List<Extension> inapplicableBeforeUpdateExtensions = new ArrayList<>();

		for (Extension.BeforeUpdateExtension extension : this.getBeforeUpdateExtensions()) {
			if (extensionStatus.skipExtensionsBefore()) { skippedBeforeUpdateExtensions.add(extension); continue; }
			ExtensionStatus returnedExtensionStatus = extension.beforeUpdate(method, updateRequest, updateState, executionState, this);
			extensionStatus.or(returnedExtensionStatus);
			if (returnedExtensionStatus == null) { inapplicableBeforeUpdateExtensions.add(extension); continue; }
			if (log.isDebugEnabled()) log.debug("Executed extension (beforeUpdate) " + extension.getClass().getSimpleName() + " with request " + updateRequest + " and state " + updateState + " and execution state " + executionState);
		}

		if (log.isDebugEnabled()) log.debug("Skipped extensions (beforeUpdate): {}, inapplicable extensions (beforeUpdate): {}", extensionClassNames(skippedBeforeUpdateExtensions), extensionClassNames(inapplicableBeforeUpdateExtensions));

		// [update]

		if (! extensionStatus.skipDriver()) {

			Driver driver = this.getDrivers().get(method);
			if (driver == null) throw new RegistrationException(RegistrationException.ERROR_BADREQUEST, "Unsupported method: " + method);
			if (log.isDebugEnabled()) log.debug("Attempting to update " + updateRequest + " with driver " + driver.getClass().getSimpleName());

			UpdateState driverUpdateState = driver.update(updateRequest);
			updateState.setJobId(driverUpdateState.getJobId());
			updateState.setDidState(driverUpdateState.getDidState());
			updateState.setDidDocumentMetadata(driverUpdateState.getDidDocumentMetadata());

			updateState.getDidRegistrationMetadata().put("method", method);
		}

		// [after update]

		List<Extension> skippedAfterUpdateExtensions = new ArrayList<>();
		List<Extension> inapplicableAfterUpdateExtensions = new ArrayList<>();

		for (Extension.AfterUpdateExtension extension : this.getAfterUpdateExtensions()) {
			if (extensionStatus.skipExtensionsAfter()) { skippedAfterUpdateExtensions.add(extension); continue; }
			ExtensionStatus returnedExtensionStatus = extension.afterUpdate(method, updateRequest, updateState, executionState, this);
			extensionStatus.or(returnedExtensionStatus);
			if (returnedExtensionStatus == null) { inapplicableAfterUpdateExtensions.add(extension); continue; }
			if (log.isDebugEnabled()) log.debug("Executed extension (afterUpdate) " + extension.getClass().getSimpleName() + " with request " + updateRequest + " and state " + updateState + " and execution state " + executionState);
		}

		if (log.isDebugEnabled()) log.debug("Skipped extensions (afterUpdate): {}, inapplicable extensions (afterUpdate): {}", extensionClassNames(skippedAfterUpdateExtensions), extensionClassNames(inapplicableAfterUpdateExtensions));

		// additional metadata

		long stop = System.currentTimeMillis();
		updateState.getDidRegistrationMetadata().put("duration", Long.valueOf(stop - start));

		// done

		return updateState;
	}

	@Override
	public DeactivateState deactivate(String method, DeactivateRequest deactivateRequest) throws RegistrationException {

		return this.deactivate(method, deactivateRequest, null);
	}

	public DeactivateState deactivate(String method, DeactivateRequest deactivateRequest, Map<String, Object> initialExecutionState) throws RegistrationException {

		if (method == null) throw new NullPointerException();
		if (deactivateRequest == null) throw new NullPointerException();

		if (this.getDrivers() == null) throw new RegistrationException("No drivers configured.");

		// start time

		long start = System.currentTimeMillis();

		// prepare execution state

		Map<String, Object> executionState = new HashMap<>();
		if (initialExecutionState != null) executionState.putAll(initialExecutionState);

		// prepare deactivate state

		DeactivateState deactivateState = DeactivateState.build();
		ExtensionStatus extensionStatus = new ExtensionStatus();

		// [before deactivate]

		List<Extension> skippedBeforeDeactivateExtensions = new ArrayList<>();
		List<Extension> inapplicableBeforeDeactivateExtensions = new ArrayList<>();

		for (Extension.BeforeDeactivateExtension extension : this.getBeforeDeactivateExtensions()) {
			if (extensionStatus.skipExtensionsBefore()) { skippedBeforeDeactivateExtensions.add(extension); continue; }
			ExtensionStatus returnedExtensionStatus = extension.beforeDeactivate(method, deactivateRequest, deactivateState, executionState, this);
			extensionStatus.or(returnedExtensionStatus);
			if (returnedExtensionStatus == null) { inapplicableBeforeDeactivateExtensions.add(extension); continue; }
			if (log.isDebugEnabled()) log.debug("Executed extension (beforeDeactivate) " + extension.getClass().getSimpleName() + " with request " + deactivateRequest + " and state " + deactivateState + " and execution state " + executionState);
		}

		if (log.isDebugEnabled()) log.debug("Skipped extensions (beforeDeactivate): {}, inapplicable extensions (beforeDeactivate): {}", extensionClassNames(skippedBeforeDeactivateExtensions), extensionClassNames(inapplicableBeforeDeactivateExtensions));

		// [deactivate]

		if (! extensionStatus.skipDriver()) {

			Driver driver = this.getDrivers().get(method);
			if (driver == null) throw new RegistrationException(RegistrationException.ERROR_BADREQUEST, "Unsupported method: " + method);
			if (log.isDebugEnabled()) log.debug("Attempting to deactivate " + deactivateRequest + " with driver " + driver.getClass().getSimpleName());

			DeactivateState driverDeactivateState = driver.deactivate(deactivateRequest);
			deactivateState.setJobId(driverDeactivateState.getJobId());
			deactivateState.setDidState(driverDeactivateState.getDidState());
			deactivateState.setDidDocumentMetadata(driverDeactivateState.getDidDocumentMetadata());

			deactivateState.getDidRegistrationMetadata().put("method", method);
		}

		// [after deactivate]

		List<Extension> skippedAfterDeactivateExtensions = new ArrayList<>();
		List<Extension> inapplicableAfterDeactivateExtensions = new ArrayList<>();

		for (Extension.AfterDeactivateExtension extension : this.getAfterDeactivateExtensions()) {
			if (extensionStatus.skipExtensionsAfter()) { skippedAfterDeactivateExtensions.add(extension); continue; }
			ExtensionStatus returnedExtensionStatus = extension.afterDeactivate(method, deactivateRequest, deactivateState, executionState, this);
			extensionStatus.or(returnedExtensionStatus);
			if (returnedExtensionStatus == null) { inapplicableAfterDeactivateExtensions.add(extension); continue; }
			if (log.isDebugEnabled()) log.debug("Executed extension (afterDeactivate) " + extension.getClass().getSimpleName() + " with request " + deactivateRequest + " and state " + deactivateState + " and execution state " + executionState);
		}

		if (log.isDebugEnabled()) log.debug("Skipped extensions (afterDeactivate): {}, inapplicable extensions (afterDeactivate): {}", extensionClassNames(skippedAfterDeactivateExtensions), extensionClassNames(inapplicableAfterDeactivateExtensions));

		// additional metadata

		long stop = System.currentTimeMillis();
		deactivateState.getDidRegistrationMetadata().put("duration", Long.valueOf(stop - start));

		// done

		return deactivateState;
	}

	@Override
	public Map<String, Map<String, Object>> properties() throws RegistrationException {

		if (this.getDrivers() == null) throw new RegistrationException("No drivers configured.");

		Map<String, Map<String, Object>> properties = new LinkedHashMap<String, Map<String, Object>> ();

		for (Entry<String, Driver> driver : this.getDrivers().entrySet()) {

			if (log.isDebugEnabled()) log.debug("Loading properties for driver " + driver.getKey() + " (" + driver.getValue().getClass().getSimpleName() + ")");

			Map<String, Object> driverProperties = driver.getValue().properties();
			if (driverProperties == null) driverProperties = Collections.emptyMap();

			properties.put(driver.getKey(), driverProperties);
		}

		// done

		if (log.isDebugEnabled()) log.debug("Loaded properties: " + properties);
		return properties;
	}

	@Override
	public Set<String> methods() throws RegistrationException {

		if (this.getDrivers() == null) throw new RegistrationException("No drivers configured.");

		Set<String> methods = this.getDrivers().keySet();

		// done

		if (log.isDebugEnabled()) log.debug("Loaded methods: " + methods);
		return methods;
	}

	/*
	 * Helper methods
	 */

	private static List<String> extensionClassNames(List<Extension> extensions) {
		return extensions.stream().map(e -> e.getClass().getSimpleName()).toList();
	}

	public List<Extension.BeforeReadCreateExtension> getBeforeReadCreateExtensions() {
		return this.getExtensions().stream().filter(Extension.BeforeReadCreateExtension.class::isInstance).map(Extension.BeforeReadCreateExtension.class::cast).toList();
	}

	public List<Extension.BeforeReadUpdateExtension> getBeforeReadUpdateExtensions() {
		return this.getExtensions().stream().filter(Extension.BeforeReadUpdateExtension.class::isInstance).map(Extension.BeforeReadUpdateExtension.class::cast).toList();
	}

	public List<Extension.BeforeReadDeactivateExtension> getBeforeReadDeactivateExtensions() {
		return this.getExtensions().stream().filter(Extension.BeforeReadDeactivateExtension.class::isInstance).map(Extension.BeforeReadDeactivateExtension.class::cast).toList();
	}

	public List<Extension.BeforeCreateExtension> getBeforeCreateExtensions() {
		return this.getExtensions().stream().filter(Extension.BeforeCreateExtension.class::isInstance).map(Extension.BeforeCreateExtension.class::cast).toList();
	}

	public List<Extension.AfterCreateExtension> getAfterCreateExtensions() {
		return this.getExtensions().stream().filter(Extension.AfterCreateExtension.class::isInstance).map(Extension.AfterCreateExtension.class::cast).toList();
	}

	public List<Extension.BeforeUpdateExtension> getBeforeUpdateExtensions() {
		return this.getExtensions().stream().filter(Extension.BeforeUpdateExtension.class::isInstance).map(Extension.BeforeUpdateExtension.class::cast).toList();
	}

	public List<Extension.AfterUpdateExtension> getAfterUpdateExtensions() {
		return this.getExtensions().stream().filter(Extension.AfterUpdateExtension.class::isInstance).map(Extension.AfterUpdateExtension.class::cast).toList();
	}

	public List<Extension.BeforeDeactivateExtension> getBeforeDeactivateExtensions() {
		return this.getExtensions().stream().filter(Extension.BeforeDeactivateExtension.class::isInstance).map(Extension.BeforeDeactivateExtension.class::cast).toList();
	}

	public List<Extension.AfterDeactivateExtension> getAfterDeactivateExtensions() {
		return this.getExtensions().stream().filter(Extension.AfterDeactivateExtension.class::isInstance).map(Extension.AfterDeactivateExtension.class::cast).toList();
	}

	public List<Extension.BeforeWriteCreateExtension> getBeforeWriteCreateExtensions() {
		return this.getExtensions().stream().filter(Extension.BeforeWriteCreateExtension.class::isInstance).map(Extension.BeforeWriteCreateExtension.class::cast).toList();
	}

	public List<Extension.BeforeWriteUpdateExtension> getBeforeWriteUpdateExtensions() {
		return this.getExtensions().stream().filter(Extension.BeforeWriteUpdateExtension.class::isInstance).map(Extension.BeforeWriteUpdateExtension.class::cast).toList();
	}

	public List<Extension.BeforeWriteDeactivateExtension> getBeforeWriteDeactivateExtensions() {
		return this.getExtensions().stream().filter(Extension.BeforeWriteDeactivateExtension.class::isInstance).map(Extension.BeforeWriteDeactivateExtension.class::cast).toList();
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
