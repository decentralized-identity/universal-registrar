package uniregistrar.driver.did.v1;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uniregistrar.RegistrationException;
import uniregistrar.driver.Driver;
import uniregistrar.request.RegisterRequest;
import uniregistrar.request.RevokeRequest;
import uniregistrar.request.UpdateRequest;
import uniregistrar.state.RegisterState;
import uniregistrar.state.RevokeState;
import uniregistrar.state.UpdateState;

public class DidV1Driver implements Driver {

	private static Logger log = LoggerFactory.getLogger(DidV1Driver.class);

	private Map<String, Object> properties;

	private String trustAnchorSeed;

	public DidV1Driver(Map<String, Object> properties) {

		this.setProperties(properties);
	}

	public DidV1Driver() {

		this.setProperties(getPropertiesFromEnvironment());
	}

	private static Map<String, Object> getPropertiesFromEnvironment() {

		if (log.isDebugEnabled()) log.debug("Loading from environment: " + System.getenv());

		Map<String, Object> properties = new HashMap<String, Object> ();

		try {

			String env_trustAnchorSeed = System.getenv("uniregistrar_driver_did_v1_trustAnchorSeed");

			if (env_trustAnchorSeed != null) properties.put("trustAnchorSeed", env_trustAnchorSeed);
		} catch (Exception ex) {

			throw new IllegalArgumentException(ex.getMessage(), ex);
		}

		return properties;
	}

	private void configureFromProperties() {

		if (log.isDebugEnabled()) log.debug("Configuring from properties: " + this.getProperties());

		try {

			String prop_trustAnchorSeed = (String) this.getProperties().get("trustAnchorSeed");

			if (prop_trustAnchorSeed != null) this.setTrustAnchorSeed(prop_trustAnchorSeed);
		} catch (Exception ex) {

			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
	}

	@Override
	public RegisterState register(RegisterRequest registerRequest) throws RegistrationException {

		// open

		if (this.getTrustAnchorSeed() == null) this.openIndy();

		// read parameters

		String network = registerRequest.getOptions() == null ? null : (String) registerRequest.getOptions().get("network");
		if (network == null || network.trim().isEmpty()) network = "_";

		// register

		throw new RuntimeException("Not implemented.");
	}

	@Override
	public UpdateState update(UpdateRequest updateRequest) throws RegistrationException {

		throw new RuntimeException("Not implemented.");
	}

	@Override
	public RevokeState revoke(RevokeRequest revokeRequest) throws RegistrationException {

		throw new RuntimeException("Not implemented.");
	}

	@Override
	public Map<String, Object> properties() {

		return this.getProperties();
	}

	private void openIndy() throws RegistrationException {

	}

	/*
	 * Getters and setters
	 */

	public Map<String, Object> getProperties() {

		return this.properties;
	}

	public void setProperties(Map<String, Object> properties) {

		this.properties = properties;
		this.configureFromProperties();
	}

	public String getTrustAnchorSeed() {

		return this.trustAnchorSeed;
	}

	public void setTrustAnchorSeed(String trustAnchorSeed) {

		this.trustAnchorSeed = trustAnchorSeed;
	}
}
