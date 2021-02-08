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

	public ExtensionStatus beforeCreate(String driverId, CreateRequest createRequest, CreateState createState, LocalUniRegistrar localUniRegistrar) throws RegistrationException;
	public ExtensionStatus beforeUpdate(String driverId, UpdateRequest updateRequest, UpdateState updateState, LocalUniRegistrar localUniRegistrar) throws RegistrationException;
	public ExtensionStatus beforeDeactivate(String driverId, DeactivateRequest deactivateRequest, DeactivateState deactivateState, LocalUniRegistrar localUniRegistrar) throws RegistrationException;
	public ExtensionStatus afterCreate(String driverId, CreateRequest createRequest, CreateState createState, LocalUniRegistrar localUniRegistrar) throws RegistrationException;
	public ExtensionStatus afterUpdate(String driverId, UpdateRequest updateRequest, UpdateState updateState, LocalUniRegistrar localUniRegistrar) throws RegistrationException;
	public ExtensionStatus afterDeactivate(String driverId, DeactivateRequest deactivateRequest, DeactivateState deactivateState, LocalUniRegistrar localUniRegistrar) throws RegistrationException;

	public abstract static class AbstractExtension implements Extension {

		@Override
		public ExtensionStatus beforeCreate(String driverId, CreateRequest createRequest, CreateState createState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {

			return null;
		}

		@Override
		public ExtensionStatus beforeUpdate(String driverId, UpdateRequest updateRequest, UpdateState updateState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {

			return null;
		}

		@Override
		public ExtensionStatus beforeDeactivate(String driverId, DeactivateRequest deactivateRequest, DeactivateState deactivateState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {

			return null;
		}

		@Override
		public ExtensionStatus afterCreate(String driverId, CreateRequest createRequest, CreateState createState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {

			return null;
		}

		@Override
		public ExtensionStatus afterUpdate(String driverId, UpdateRequest updateRequest, UpdateState updateState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {

			return null;
		}

		@Override
		public ExtensionStatus afterDeactivate(String driverId, DeactivateRequest deactivateRequest, DeactivateState deactivateState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {

			return null;
		}
	}
}
