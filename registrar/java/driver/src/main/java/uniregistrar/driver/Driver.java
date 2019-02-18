package uniregistrar.driver;

import java.util.Map;

import uniregistrar.RegistrationException;
import uniregistrar.request.RegisterRequest;
import uniregistrar.request.RevokeRequest;
import uniregistrar.request.UpdateRequest;
import uniregistrar.state.RegisterState;
import uniregistrar.state.RevokeState;
import uniregistrar.state.UpdateState;

public interface Driver {

	public static final String PROPERTIES_MIME_TYPE = "application/json";

	public RegisterState register(RegisterRequest registerRequest) throws RegistrationException;
	public UpdateState update(UpdateRequest updateRequest) throws RegistrationException;
	public RevokeState revoke(RevokeRequest revokeRequest) throws RegistrationException;
	public Map<String, Object> properties() throws RegistrationException;
}
