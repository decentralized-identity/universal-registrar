package uniregistrar.driver;

import uniregistrar.RegistrationException;
import uniregistrar.openapi.model.*;

import java.util.Collections;
import java.util.Map;

public interface Driver {

	public static final String PROPERTIES_MIME_TYPE = "application/json";

	public CreateState create(CreateRequest createRequest) throws RegistrationException;
	public UpdateState update(UpdateRequest updateRequest) throws RegistrationException;
	public DeactivateState deactivate(DeactivateRequest deactivateRequest) throws RegistrationException;

	default public Map<String, Object> properties() throws RegistrationException {

		return Collections.emptyMap();
	}
}
