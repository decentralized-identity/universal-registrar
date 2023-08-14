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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;

public interface Extension {

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@interface ExtensionStage {
		String value();
	}

	@FunctionalInterface
	interface ExtensionFunction<E extends Extension> {
		ExtensionStatus apply(E extension) throws RegistrationException;
	}

	@FunctionalInterface
	interface ExtensionFunctionVoid<E extends Extension> {
		void apply(E extension) throws RegistrationException;
	}

	@ExtensionStage("beforeReadCreate")
	interface BeforeReadCreateExtension extends Extension {
		default void beforeReadCreate(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		}
	}

	@ExtensionStage("beforeReadUpdate")
	interface BeforeReadUpdateExtension extends Extension {
		default void beforeReadUpdate(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		}
	}

	@ExtensionStage("beforeReadDeactivate")
	interface BeforeReadDeactivateExtension extends Extension {
		default void beforeReadDeactivate(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		}
	}

	@ExtensionStage("beforeCreate")
	interface BeforeCreateExtension extends Extension {
		default ExtensionStatus beforeCreate(String method, CreateRequest createRequest, CreateState createState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return null;
		}
	}

	@ExtensionStage("beforeUpdate")
	interface BeforeUpdateExtension extends Extension {
		default ExtensionStatus beforeUpdate(String method, UpdateRequest updateRequest, UpdateState updateState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return null;
		}
	}

	@ExtensionStage("beforeDeactivate")
	interface BeforeDeactivateExtension extends Extension {
		default ExtensionStatus beforeDeactivate(String method, DeactivateRequest deactivateRequest, DeactivateState deactivateState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return null;
		}
	}

	@ExtensionStage("afterCreate")
	interface AfterCreateExtension extends Extension {
		default ExtensionStatus afterCreate(String method, CreateRequest createRequest, CreateState createState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return null;
		}
	}

	@ExtensionStage("afterUpdate")
	interface AfterUpdateExtension extends Extension {
		default ExtensionStatus afterUpdate(String method, UpdateRequest updateRequest, UpdateState updateState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return null;
		}
	}

	@ExtensionStage("afterDeactivate")
	interface AfterDeactivateExtension extends Extension {
		default ExtensionStatus afterDeactivate(String method, DeactivateRequest deactivateRequest, DeactivateState deactivateState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return null;
		}
	}

	@ExtensionStage("beforeWriteCreate")
	interface BeforeWriteCreateExtension extends Extension {
		default void beforeWriteCreate(String method, Map<String, Object> stateMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		}
	}

	@ExtensionStage("beforeWriteUpdate")
	interface BeforeWriteUpdateExtension extends Extension {
		default void beforeWriteUpdate(String method, Map<String, Object> stateMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		}
	}

	@ExtensionStage("beforeWriteDeactivate")
	interface BeforeWriteDeactivateExtension extends Extension {
		default void beforeWriteDeactivate(String method, Map<String, Object> stateMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		}
	}

	abstract class AbstractExtension implements BeforeReadCreateExtension, BeforeReadUpdateExtension, BeforeReadDeactivateExtension, BeforeCreateExtension, BeforeUpdateExtension, BeforeDeactivateExtension, AfterCreateExtension, AfterUpdateExtension, AfterDeactivateExtension, BeforeWriteCreateExtension, BeforeWriteUpdateExtension, BeforeWriteDeactivateExtension {
	}

	abstract class AbstractBeforeReadExtension implements Extension.BeforeReadCreateExtension, Extension.BeforeReadUpdateExtension, Extension.BeforeReadDeactivateExtension {

		@Override
		public final void beforeReadCreate(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			this.beforeRead(method, requestMap, localUniRegistrar);
		}

		@Override
		public final void beforeReadUpdate(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			this.beforeRead(method, requestMap, localUniRegistrar);
		}

		@Override
		public final void beforeReadDeactivate(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			this.beforeRead(method, requestMap, localUniRegistrar);
		}

		abstract public void beforeRead(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException;
	}

	abstract class AbstractBeforeRequestExtension implements Extension.BeforeCreateExtension, Extension.BeforeUpdateExtension, Extension.BeforeDeactivateExtension {

		@Override
		public final ExtensionStatus beforeCreate(String method, CreateRequest createRequest, CreateState createState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.beforeRequest(method, createRequest, createState, executionState, localUniRegistrar);
		}

		@Override
		public final ExtensionStatus beforeUpdate(String method, UpdateRequest updateRequest, UpdateState updateState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.beforeRequest(method, updateRequest, updateState, executionState, localUniRegistrar);
		}

		@Override
		public final ExtensionStatus beforeDeactivate(String method, DeactivateRequest deactivateRequest, DeactivateState deactivateState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.beforeRequest(method, deactivateRequest, deactivateState, executionState, localUniRegistrar);
		}

		abstract public ExtensionStatus beforeRequest(String method, Request request, State state, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException;
	}

	abstract class AbstractAfterRequestExtension implements Extension.AfterCreateExtension, Extension.AfterUpdateExtension, Extension.AfterDeactivateExtension {

		@Override
		public final ExtensionStatus afterCreate(String method, CreateRequest createRequest, CreateState createState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.afterRequest(method, createRequest, createState, executionState, localUniRegistrar);
		}

		@Override
		public final ExtensionStatus afterUpdate(String method, UpdateRequest updateRequest, UpdateState updateState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.afterRequest(method, updateRequest, updateState, executionState, localUniRegistrar);
		}

		@Override
		public final ExtensionStatus afterDeactivate(String method, DeactivateRequest deactivateRequest, DeactivateState deactivateState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.afterRequest(method, deactivateRequest, deactivateState, executionState, localUniRegistrar);
		}

		abstract public ExtensionStatus afterRequest(String method, Request request, State state, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException;
	}

	abstract class AbstractBeforeWriteExtension implements Extension.BeforeWriteCreateExtension, Extension.BeforeWriteUpdateExtension, Extension.BeforeWriteDeactivateExtension {

		@Override
		public final void beforeWriteCreate(String method, Map<String, Object> stateMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			this.beforeWrite(method, stateMap, localUniRegistrar);
		}

		@Override
		public final void beforeWriteUpdate(String method, Map<String, Object> stateMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			this.beforeWrite(method, stateMap, localUniRegistrar);
		}

		@Override
		public final void beforeWriteDeactivate(String method, Map<String, Object> stateMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			this.beforeWrite(method, stateMap, localUniRegistrar);
		}

		abstract public void beforeWrite(String method, Map<String, Object> stateMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException;
	}

	abstract class AbstractRequestExtension implements Extension.BeforeCreateExtension, Extension.BeforeUpdateExtension, Extension.BeforeDeactivateExtension, Extension.AfterCreateExtension, Extension.AfterUpdateExtension, Extension.AfterDeactivateExtension {

		@Override
		public final ExtensionStatus beforeCreate(String method, CreateRequest createRequest, CreateState createState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.beforeRequest(method, createRequest, createState, executionState, localUniRegistrar);
		}

		@Override
		public final ExtensionStatus beforeUpdate(String method, UpdateRequest updateRequest, UpdateState updateState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.beforeRequest(method, updateRequest, updateState, executionState, localUniRegistrar);
		}

		@Override
		public final ExtensionStatus beforeDeactivate(String method, DeactivateRequest deactivateRequest, DeactivateState deactivateState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.beforeRequest(method, deactivateRequest, deactivateState, executionState, localUniRegistrar);
		}

		@Override
		public final ExtensionStatus afterCreate(String method, CreateRequest createRequest, CreateState createState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.afterRequest(method, createRequest, createState, executionState, localUniRegistrar);
		}

		@Override
		public final ExtensionStatus afterUpdate(String method, UpdateRequest updateRequest, UpdateState updateState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.afterRequest(method, updateRequest, updateState, executionState, localUniRegistrar);
		}

		@Override
		public final ExtensionStatus afterDeactivate(String method, DeactivateRequest deactivateRequest, DeactivateState deactivateState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.afterRequest(method, deactivateRequest, deactivateState, executionState, localUniRegistrar);
		}

		abstract public ExtensionStatus beforeRequest(String method, Request request, State state, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException;

		abstract public ExtensionStatus afterRequest(String method, Request request, State state, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException;
	}

	static List<String> extensionClassNames(List<? extends Extension> extensions) {
		return extensions.stream().map(e -> e.getClass().getSimpleName()).toList();
	}
}
