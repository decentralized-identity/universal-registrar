package uniregistrar.driver;

import java.util.Map;

import uniregistrar.RegistrationException;
import uniregistrar.request.RegisterRequest;
import uniregistrar.request.DeactivateRequest;
import uniregistrar.request.UpdateRequest;
import uniregistrar.state.RegisterState;
import uniregistrar.state.DeactivateState;
import uniregistrar.state.UpdateState;

public interface Driver {

	public static final String PROPERTIES_MIME_TYPE = "application/json";

	public RegisterState register(RegisterRequest registerRequest) throws RegistrationException;
	public UpdateState update(UpdateRequest updateRequest) throws RegistrationException;
	public DeactivateState deactivate(DeactivateRequest deactivateRequest) throws RegistrationException;
	public Map<String, Object> properties() throws RegistrationException;
}
