package uniregistrar.local.extensions;

import uniregistrar.RegistrationException;
import uniregistrar.local.LocalUniRegistrar;
import uniregistrar.request.DeactivateRequest;
import uniregistrar.request.CreateRequest;
import uniregistrar.request.UpdateRequest;
import uniregistrar.state.DeactivateState;
import uniregistrar.state.CreateState;
import uniregistrar.state.UpdateState;

import java.util.Map;

public interface Extension {

	public default ExtensionStatus beforeCreate(String method, CreateRequest createRequest, CreateState createState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		return null;
	}

	public default ExtensionStatus beforeUpdate(String method, UpdateRequest updateRequest, UpdateState updateState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		return null;
	}

	public default ExtensionStatus beforeDeactivate(String method, DeactivateRequest deactivateRequest, DeactivateState deactivateState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		return null;
	}

	public default ExtensionStatus afterCreate(String method, CreateRequest createRequest, CreateState createState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		return null;
	}

	public default ExtensionStatus afterUpdate(String method, UpdateRequest updateRequest, UpdateState updateState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		return null;
	}

	public default ExtensionStatus afterDeactivate(String method, DeactivateRequest deactivateRequest, DeactivateState deactivateState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		return null;
	}

	public abstract static class AbstractExtension implements Extension {
	}
}
