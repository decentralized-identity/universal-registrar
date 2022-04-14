package uniregistrar.local.extensions;

import uniregistrar.RegistrationException;
import uniregistrar.local.LocalUniRegistrar;
import uniregistrar.request.CreateRequest;
import uniregistrar.request.DeactivateRequest;
import uniregistrar.request.Request;
import uniregistrar.request.UpdateRequest;
import uniregistrar.state.CreateState;
import uniregistrar.state.DeactivateState;
import uniregistrar.state.State;
import uniregistrar.state.UpdateState;

import java.util.Map;

public interface Extension {

	default ExtensionStatus beforeCreate(String method, CreateRequest createRequest, CreateState createState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		return null;
	}

	default ExtensionStatus beforeUpdate(String method, UpdateRequest updateRequest, UpdateState updateState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		return null;
	}

	default ExtensionStatus beforeDeactivate(String method, DeactivateRequest deactivateRequest, DeactivateState deactivateState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		return null;
	}

	default ExtensionStatus afterCreate(String method, CreateRequest createRequest, CreateState createState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		return null;
	}

	default ExtensionStatus afterUpdate(String method, UpdateRequest updateRequest, UpdateState updateState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		return null;
	}

	default ExtensionStatus afterDeactivate(String method, DeactivateRequest deactivateRequest, DeactivateState deactivateState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		return null;
	}

	abstract class AbstractExtension implements Extension {
	}

	abstract class AbstractRequestExtension extends AbstractExtension implements Extension {

		public final ExtensionStatus beforeCreate(String method, CreateRequest createRequest, CreateState createState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.beforeRequest(method, createRequest, createState, executionState, localUniRegistrar);
		}

		public final ExtensionStatus beforeUpdate(String method, UpdateRequest updateRequest, UpdateState updateState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.beforeRequest(method, updateRequest, updateState, executionState, localUniRegistrar);
		}

		public final ExtensionStatus beforeDeactivate(String method, DeactivateRequest deactivateRequest, DeactivateState deactivateState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.beforeRequest(method, deactivateRequest, deactivateState, executionState, localUniRegistrar);
		}

		public final ExtensionStatus afterCreate(String method, CreateRequest createRequest, CreateState createState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.afterRequest(method, createRequest, createState, executionState, localUniRegistrar);
		}

		public final ExtensionStatus afterUpdate(String method, UpdateRequest updateRequest, UpdateState updateState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.afterRequest(method, updateRequest, updateState, executionState, localUniRegistrar);
		}

		public final ExtensionStatus afterDeactivate(String method, DeactivateRequest deactivateRequest, DeactivateState deactivateState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.afterRequest(method, deactivateRequest, deactivateState, executionState, localUniRegistrar);
		}

		public ExtensionStatus beforeRequest(String method, Request request, State state, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return null;
		}

		public ExtensionStatus afterRequest(String method, Request request, State state, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return null;
		}
	}
}
