package uniregistrar;

import uniregistrar.openapi.model.*;

import java.util.Map;
import java.util.Set;

public interface ExecutionStateUniRegistrar extends UniRegistrar {

	public CreateState create(String method, CreateRequest createRequest, Map<String, Object> executionState) throws RegistrationException;
	public UpdateState update(String method, UpdateRequest updateRequest, Map<String, Object> executionState) throws RegistrationException;
	public DeactivateState deactivate(String method, DeactivateRequest deactivateRequest, Map<String, Object> executionState) throws RegistrationException;
	public ExecuteState execute(String method, ExecuteRequest executeRequest, Map<String, Object> executionState) throws RegistrationException;
	public CreateResourceState createResource(String method, CreateResourceRequest createResourceRequest, Map<String, Object> executionState) throws RegistrationException;
	public UpdateResourceState updateResource(String method, UpdateResourceRequest updateResourceRequest, Map<String, Object> executionState) throws RegistrationException;
	public DeactivateResourceState deactivateResource(String method, DeactivateResourceRequest deactivateResourceRequest, Map<String, Object> executionState) throws RegistrationException;
}
