package uniregistrar;

import uniregistrar.openapi.model.*;

import java.util.Map;
import java.util.Set;

public interface UniRegistrar {

	public static final String PROPERTIES_MIME_TYPE = "application/json";
	public static final String METHODS_MIME_TYPE = "application/json";

	public CreateState create(String method, CreateRequest createRequest) throws RegistrationException;
	public UpdateState update(String method, UpdateRequest updateRequest) throws RegistrationException;
	public DeactivateState deactivate(String method, DeactivateRequest deactivateRequest) throws RegistrationException;
	public ExecuteState execute(String method, ExecuteRequest executeRequest) throws RegistrationException;
	public Map<String, Map<String, Object>> properties() throws RegistrationException;
	public Set<String> methods() throws RegistrationException;
}
