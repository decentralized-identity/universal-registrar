package uniregistrar;

import java.util.Map;

import uniregistrar.request.CreateRequest;
import uniregistrar.request.DeactivateRequest;
import uniregistrar.request.UpdateRequest;
import uniregistrar.state.CreateState;
import uniregistrar.state.DeactivateState;
import uniregistrar.state.UpdateState;

public interface UniRegistrar {

	public static final String PROPERTIES_MIME_TYPE = "application/json";

	public CreateState create(String method, CreateRequest createRequest) throws RegistrationException;
	public UpdateState update(String method, UpdateRequest updateRequest) throws RegistrationException;
	public DeactivateState deactivate(String method, DeactivateRequest deactivateRequest) throws RegistrationException;
	public Map<String, Map<String, Object>> properties() throws RegistrationException;
}
