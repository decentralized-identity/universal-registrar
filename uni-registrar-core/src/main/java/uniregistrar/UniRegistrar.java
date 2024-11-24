package uniregistrar;

import uniregistrar.openapi.model.*;

import java.util.Map;
import java.util.Set;

public interface UniRegistrar {

	public static final String PROPERTIES_MIME_TYPE = "application/json";
	public static final String METHODS_MIME_TYPE = "application/json";
	public static final String TRAITS_MIME_TYPE = "application/json";

	public CreateState create(String method, CreateRequest createRequest) throws RegistrationException;
	public UpdateState update(String method, UpdateRequest updateRequest) throws RegistrationException;
	public DeactivateState deactivate(String method, DeactivateRequest deactivateRequest) throws RegistrationException;
	public ExecuteState execute(String method, ExecuteRequest executeRequest) throws RegistrationException;
	public CreateResourceState createResource(String method, CreateResourceRequest createResourceRequest) throws RegistrationException;
	public UpdateResourceState updateResource(String method, UpdateResourceRequest updateResourceRequest) throws RegistrationException;
	public DeactivateResourceState deactivateResource(String method, DeactivateResourceRequest deactivateResourceRequest) throws RegistrationException;

	public Map<String, Map<String, Object>> properties() throws RegistrationException;
	public Set<String> methods() throws RegistrationException;
	public Map<String, Map<String, Object>> traits() throws RegistrationException;
}
