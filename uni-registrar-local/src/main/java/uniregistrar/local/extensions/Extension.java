package uniregistrar.local.extensions;

import uniregistrar.RegistrationException;
import uniregistrar.local.LocalUniRegistrar;
import uniregistrar.openapi.model.*;

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

	/*
	 * beforeRead
	 */

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

	/*
	 * before
	 */

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

	/*
	 * beforeDriverWrite
	 */

	@ExtensionStage("beforeDriverWriteCreate")
	interface BeforeDriverWriteCreateExtension extends Extension {
		default void beforeDriverWriteCreate(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		}
	}

	@ExtensionStage("beforeDriverWriteUpdate")
	interface BeforeDriverWriteUpdateExtension extends Extension {
		default void beforeDriverWriteUpdate(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		}
	}

	@ExtensionStage("beforeDriverWriteDeactivate")
	interface BeforeDriverWriteDeactivateExtension extends Extension {
		default void beforeDriverWriteDeactivate(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		}
	}

	/*
	 * beforeDriverRead
	 */

	@ExtensionStage("beforeDriverReadCreate")
	interface BeforeDriverReadCreateExtension extends Extension {
		default void beforeDriverReadCreate(String method, Map<String, Object> stateMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		}
	}

	@ExtensionStage("beforeDriverReadUpdate")
	interface BeforeDriverReadUpdateExtension extends Extension {
		default void beforeDriverReadUpdate(String method, Map<String, Object> stateMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		}
	}

	@ExtensionStage("beforeDriverReadDeactivate")
	interface BeforeDriverReadDeactivateExtension extends Extension {
		default void beforeDriverReadDeactivate(String method, Map<String, Object> stateMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		}
	}

	/*
	 * after
	 */

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

	/*
	 * beforeWrite
	 */

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

	/*
	 * Helper classes
	 */

	abstract class AbstractExtension implements BeforeReadCreateExtension, BeforeReadUpdateExtension, BeforeReadDeactivateExtension, BeforeCreateExtension, BeforeUpdateExtension, BeforeDeactivateExtension, BeforeDriverWriteCreateExtension, BeforeDriverWriteUpdateExtension, BeforeDriverWriteDeactivateExtension, BeforeDriverReadCreateExtension, BeforeDriverReadUpdateExtension, BeforeDriverReadDeactivateExtension, AfterCreateExtension, AfterUpdateExtension, AfterDeactivateExtension, BeforeWriteCreateExtension, BeforeWriteUpdateExtension, BeforeWriteDeactivateExtension {
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

		abstract public ExtensionStatus beforeRequest(String method, RegistrarRequest request, RegistrarState state, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException;
	}

	abstract class AbstractBeforeDriverWriteExtension implements Extension.BeforeDriverWriteCreateExtension, Extension.BeforeDriverWriteUpdateExtension, Extension.BeforeDriverWriteDeactivateExtension {

		@Override
		public final void beforeDriverWriteCreate(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			this.beforeDriverWrite(method, requestMap, localUniRegistrar);
		}

		@Override
		public final void beforeDriverWriteUpdate(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			this.beforeDriverWrite(method, requestMap, localUniRegistrar);
		}

		@Override
		public final void beforeDriverWriteDeactivate(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			this.beforeDriverWrite(method, requestMap, localUniRegistrar);
		}

		abstract public void beforeDriverWrite(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException;
	}

	abstract class AbstractBeforeDriverReadExtension implements Extension.BeforeDriverReadCreateExtension, Extension.BeforeDriverReadUpdateExtension, Extension.BeforeDriverReadDeactivateExtension {

		@Override
		public final void beforeDriverReadCreate(String method, Map<String, Object> stateMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			this.beforeDriverRead(method, stateMap, localUniRegistrar);
		}

		@Override
		public final void beforeDriverReadUpdate(String method, Map<String, Object> stateMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			this.beforeDriverRead(method, stateMap, localUniRegistrar);
		}

		@Override
		public final void beforeDriverReadDeactivate(String method, Map<String, Object> stateMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			this.beforeDriverRead(method, stateMap, localUniRegistrar);
		}

		abstract public void beforeDriverRead(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException;
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

		abstract public ExtensionStatus afterRequest(String method, RegistrarRequest request, RegistrarState state, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException;
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

		abstract public ExtensionStatus beforeRequest(String method, RegistrarRequest request, RegistrarState state, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException;

		abstract public ExtensionStatus afterRequest(String method, RegistrarRequest request, RegistrarState state, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException;
	}

	static List<String> extensionClassNames(List<? extends Extension> extensions) {
		return extensions.stream().map(e -> e.getClass().getSimpleName()).toList();
	}
}
