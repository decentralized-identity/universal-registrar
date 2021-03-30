package uniregistrar.local.extensions;

import uniregistrar.RegistrationException;
import uniregistrar.local.LocalUniRegistrar;
import uniregistrar.request.DeactivateRequest;
import uniregistrar.request.CreateRequest;
import uniregistrar.request.UpdateRequest;
import uniregistrar.state.DeactivateState;
import uniregistrar.state.CreateState;
import uniregistrar.state.UpdateState;

public interface Extension {

	public ExtensionStatus beforeCreate(String method, CreateRequest createRequest, CreateState createState, LocalUniRegistrar localUniRegistrar) throws RegistrationException;
	public ExtensionStatus beforeUpdate(String method, UpdateRequest updateRequest, UpdateState updateState, LocalUniRegistrar localUniRegistrar) throws RegistrationException;
	public ExtensionStatus beforeDeactivate(String method, DeactivateRequest deactivateRequest, DeactivateState deactivateState, LocalUniRegistrar localUniRegistrar) throws RegistrationException;
	public ExtensionStatus afterCreate(String method, CreateRequest createRequest, CreateState createState, LocalUniRegistrar localUniRegistrar) throws RegistrationException;
	public ExtensionStatus afterUpdate(String method, UpdateRequest updateRequest, UpdateState updateState, LocalUniRegistrar localUniRegistrar) throws RegistrationException;
	public ExtensionStatus afterDeactivate(String method, DeactivateRequest deactivateRequest, DeactivateState deactivateState, LocalUniRegistrar localUniRegistrar) throws RegistrationException;

	public abstract static class AbstractExtension implements Extension {

		@Override
		public ExtensionStatus beforeCreate(String method, CreateRequest createRequest, CreateState createState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {

			return null;
		}

		@Override
		public ExtensionStatus beforeUpdate(String method, UpdateRequest updateRequest, UpdateState updateState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {

			return null;
		}

		@Override
		public ExtensionStatus beforeDeactivate(String method, DeactivateRequest deactivateRequest, DeactivateState deactivateState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {

			return null;
		}

		@Override
		public ExtensionStatus afterCreate(String method, CreateRequest createRequest, CreateState createState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {

			return null;
		}

		@Override
		public ExtensionStatus afterUpdate(String method, UpdateRequest updateRequest, UpdateState updateState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {

			return null;
		}

		@Override
		public ExtensionStatus afterDeactivate(String method, DeactivateRequest deactivateRequest, DeactivateState deactivateState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {

			return null;
		}
	}
}
