package uniregistrar.local.extensions;

import uniregistrar.RegistrationException;
import uniregistrar.local.LocalUniRegistrar;
import uniregistrar.request.DeactivateRequest;
import uniregistrar.request.RegisterRequest;
import uniregistrar.request.UpdateRequest;
import uniregistrar.state.DeactivateState;
import uniregistrar.state.RegisterState;
import uniregistrar.state.UpdateState;

public interface Extension {

	public ExtensionStatus beforeRegister(String driverId, RegisterRequest registerRequest, RegisterState registerState, LocalUniRegistrar localUniRegistrar) throws RegistrationException;
	public ExtensionStatus beforeUpdate(String driverId, UpdateRequest updateRequest, UpdateState updateState, LocalUniRegistrar localUniRegistrar) throws RegistrationException;
	public ExtensionStatus beforeDeactivate(String driverId, DeactivateRequest deactivateRequest, DeactivateState deactivateState, LocalUniRegistrar localUniRegistrar) throws RegistrationException;
	public ExtensionStatus afterRegister(String driverId, RegisterRequest registerRequest, RegisterState registerState, LocalUniRegistrar localUniRegistrar) throws RegistrationException;
	public ExtensionStatus afterUpdate(String driverId, UpdateRequest updateRequest, UpdateState updateState, LocalUniRegistrar localUniRegistrar) throws RegistrationException;
	public ExtensionStatus afterDeactivate(String driverId, DeactivateRequest deactivateRequest, DeactivateState deactivateState, LocalUniRegistrar localUniRegistrar) throws RegistrationException;

	public abstract static class AbstractExtension implements Extension {

		@Override
		public ExtensionStatus beforeRegister(String driverId, RegisterRequest registerRequest, RegisterState registerState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {

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
		public ExtensionStatus afterRegister(String driverId, RegisterRequest registerRequest, RegisterState registerState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {

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
