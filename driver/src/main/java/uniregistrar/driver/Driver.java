package uniregistrar.driver;

import uniregistrar.RegistrationException;
import uniregistrar.openapi.model.*;

import java.util.Collections;
import java.util.Map;

public interface Driver {

	public static final String PROPERTIES_MEDIA_TYPE = "application/json";
	public static final String TRAITS_MEDIA_TYPE = "application/json";

	default public CreateState create(CreateRequest createRequest) throws RegistrationException {
		throw new RegistrationException("Driver does not support create");
	}
	default public UpdateState update(UpdateRequest updateRequest) throws RegistrationException {
		throw new RegistrationException("Driver does not support update");
	}
	default public DeactivateState deactivate(DeactivateRequest deactivateRequest) throws RegistrationException {
		throw new RegistrationException("Driver does not support deactivate");
	}
	default public ExecuteState execute(ExecuteRequest executeRequest) throws RegistrationException {
		throw new RegistrationException("Driver does not support execute");
	}
	default public CreateResourceState createResource(CreateResourceRequest createResourceRequest) throws RegistrationException {
		throw new RegistrationException("Driver does not support createResource");
	}
	default public UpdateResourceState updateResource(UpdateResourceRequest updateResourceRequest) throws RegistrationException {
		throw new RegistrationException("Driver does not support updateResource");
	}
	default public DeactivateResourceState deactivateResource(DeactivateResourceRequest deactivateResourceRequest) throws RegistrationException {
		throw new RegistrationException("Driver does not support deactivateResource");
	}

	default public Map<String, Object> properties() throws RegistrationException {
		return Collections.emptyMap();
	}

	default public Map<String, Object> traits() throws RegistrationException {
		return Collections.emptyMap();
	}
}
