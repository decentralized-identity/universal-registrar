package uniregistrar.local;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uniregistrar.RegistrationException;
import uniregistrar.UniRegistrar;
import uniregistrar.driver.Driver;
import uniregistrar.request.DeactivateRequest;
import uniregistrar.request.RegisterRequest;
import uniregistrar.request.UpdateRequest;
import uniregistrar.state.DeactivateState;
import uniregistrar.state.RegisterState;
import uniregistrar.state.UpdateState;

public class LocalUniRegistrar implements UniRegistrar {

	private static Logger log = LoggerFactory.getLogger(LocalUniRegistrar.class);

	private static final LocalUniRegistrar DEFAULT_REGISTRAR;

	private Map<String, Driver> drivers;

	static {

		DEFAULT_REGISTRAR = new LocalUniRegistrar();
	}

	public static LocalUniRegistrar getDefault() {

		return DEFAULT_REGISTRAR;
	}

	public LocalUniRegistrar() {

	}

	@Override
	public RegisterState register(String driverId, RegisterRequest registerRequest) throws RegistrationException {

		if (driverId == null) throw new NullPointerException();
		if (registerRequest == null) throw new NullPointerException();

		if (this.getDrivers() == null) throw new RegistrationException("No drivers configured.");

		// start time

		long start = System.currentTimeMillis();

		// select driver

		Driver driver = this.getDrivers().get(driverId);
		if (driver == null) throw new RegistrationException("Unknown driver: " + driverId);
		if (log.isDebugEnabled()) log.debug("Attemping to register " + registerRequest + " with driver " + driver.getClass());

		RegisterState registerState = driver.register(registerRequest);

		// stop time

		long stop = System.currentTimeMillis();

		// add REGISTRAR METADATA

		Map<String, Object> registrarMetadata = new LinkedHashMap<String, Object> ();
		registrarMetadata.put("driverId", driverId);
		registrarMetadata.put("driver", driver.getClass().getSimpleName());
		registrarMetadata.put("duration", Long.valueOf(stop - start));

		registerState.setRegistrarMetadata(registrarMetadata);

		// done

		return registerState;
	}

	@Override
	public UpdateState update(String driverId, UpdateRequest updateRequest) throws RegistrationException {

		if (driverId == null) throw new NullPointerException();
		if (updateRequest == null) throw new NullPointerException();

		if (this.getDrivers() == null) throw new RegistrationException("No drivers configured.");

		// start time

		long start = System.currentTimeMillis();

		// select driver

		Driver driver = this.getDrivers().get(driverId);
		if (driver == null) throw new RegistrationException("Unknown driver: " + driverId);
		if (log.isDebugEnabled()) log.debug("Attemping to update " + updateRequest + " with driver " + driver.getClass());

		UpdateState updateState = driver.update(updateRequest);

		// stop time

		long stop = System.currentTimeMillis();

		// add REGISTRAR METADATA

		Map<String, Object> registrarMetadata = new LinkedHashMap<String, Object> ();
		registrarMetadata.put("driverId", driverId);
		registrarMetadata.put("driver", driver.getClass().getSimpleName());
		registrarMetadata.put("duration", Long.valueOf(stop - start));

		updateState.setRegistrarMetadata(registrarMetadata);

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

		// select driver

		Driver driver = this.getDrivers().get(driverId);
		if (driver == null) throw new RegistrationException("Unknown driver: " + driverId);
		if (log.isDebugEnabled()) log.debug("Attemping to deactivate " + deactivateRequest + " with driver " + driver.getClass());

		DeactivateState deactivateState = driver.deactivate(deactivateRequest);

		// stop time

		long stop = System.currentTimeMillis();

		// add REGISTRAR METADATA

		Map<String, Object> registrarMetadata = new LinkedHashMap<String, Object> ();
		registrarMetadata.put("driverId", driverId);
		registrarMetadata.put("driver", driver.getClass().getSimpleName());
		registrarMetadata.put("duration", Long.valueOf(stop - start));

		deactivateState.setRegistrarMetadata(registrarMetadata);

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
}
