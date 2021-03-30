package uniregistrar.driver;

import java.util.Collections;
import java.util.Map;

import uniregistrar.RegistrationException;
import uniregistrar.request.CreateRequest;
import uniregistrar.request.DeactivateRequest;
import uniregistrar.request.UpdateRequest;
import uniregistrar.state.CreateState;
import uniregistrar.state.DeactivateState;
import uniregistrar.state.UpdateState;

public interface Driver {

	public static final String PROPERTIES_MIME_TYPE = "application/json";

	public CreateState create(CreateRequest createRequest) throws RegistrationException;
	public UpdateState update(UpdateRequest updateRequest) throws RegistrationException;
	public DeactivateState deactivate(DeactivateRequest deactivateRequest) throws RegistrationException;

	default public Map<String, Object> properties() throws RegistrationException {

		return Collections.emptyMap();
	}
}
