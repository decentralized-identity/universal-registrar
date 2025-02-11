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

	@ExtensionStage("beforeReadExecute")
	interface BeforeReadExecuteExtension extends Extension {
		default void beforeReadExecute(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		}
	}

	@ExtensionStage("beforeReadCreateResource")
	interface BeforeReadCreateResourceExtension extends Extension {
		default void beforeReadCreateResource(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		}
	}

	@ExtensionStage("beforeReadUpdateResource")
	interface BeforeReadUpdateResourceExtension extends Extension {
		default void beforeReadUpdateResource(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		}
	}

	@ExtensionStage("beforeReadDeactivateResource")
	interface BeforeReadDeactivateResourceExtension extends Extension {
		default void beforeReadDeactivateResource(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
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

	@ExtensionStage("beforeExecute")
	interface BeforeExecuteExtension extends Extension {
		default ExtensionStatus beforeExecute(String method, ExecuteRequest executeRequest, ExecuteState executeState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return null;
		}
	}

	@ExtensionStage("beforeCreateResource")
	interface BeforeCreateResourceExtension extends Extension {
		default ExtensionStatus beforeCreateResource(String method, CreateResourceRequest createResourceRequest, CreateResourceState createResourceState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return null;
		}
	}

	@ExtensionStage("beforeUpdateResource")
	interface BeforeUpdateResourceExtension extends Extension {
		default ExtensionStatus beforeUpdateResource(String method, UpdateResourceRequest updateResourceRequest, UpdateResourceState updateResourceState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return null;
		}
	}

	@ExtensionStage("beforeDeactivateResource")
	interface BeforeDeactivateResourceExtension extends Extension {
		default ExtensionStatus beforeDeactivateResource(String method, DeactivateResourceRequest deactivateResourceRequest, DeactivateResourceState deactivateResourceState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
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

	@ExtensionStage("beforeDriverWriteExecute")
	interface BeforeDriverWriteExecuteExtension extends Extension {
		default void beforeDriverWriteExecute(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		}
	}

	@ExtensionStage("beforeDriverWriteCreateResource")
	interface BeforeDriverWriteCreateResourceExtension extends Extension {
		default void beforeDriverWriteCreateResource(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		}
	}

	@ExtensionStage("beforeDriverWriteUpdateResource")
	interface BeforeDriverWriteUpdateResourceExtension extends Extension {
		default void beforeDriverWriteUpdateResource(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		}
	}

	@ExtensionStage("beforeDriverWriteDeactivateResource")
	interface BeforeDriverWriteDeactivateResourceExtension extends Extension {
		default void beforeDriverWriteDeactivateResource(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
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

	@ExtensionStage("beforeDriverReadExecute")
	interface BeforeDriverReadExecuteExtension extends Extension {
		default void beforeDriverReadExecute(String method, Map<String, Object> stateMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		}
	}

	@ExtensionStage("beforeDriverReadCreateResource")
	interface BeforeDriverReadCreateResourceExtension extends Extension {
		default void beforeDriverReadCreateResource(String method, Map<String, Object> stateMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		}
	}

	@ExtensionStage("beforeDriverReadUpdateResource")
	interface BeforeDriverReadUpdateResourceExtension extends Extension {
		default void beforeDriverReadUpdateResource(String method, Map<String, Object> stateMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		}
	}

	@ExtensionStage("beforeDriverReadDeactivateResource")
	interface BeforeDriverReadDeactivateResourceExtension extends Extension {
		default void beforeDriverReadDeactivateResource(String method, Map<String, Object> stateMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
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

	@ExtensionStage("afterExecute")
	interface AfterExecuteExtension extends Extension {
		default ExtensionStatus afterExecute(String method, ExecuteRequest executeRequest, ExecuteState executeState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return null;
		}
	}

	@ExtensionStage("afterCreateResource")
	interface AfterCreateResourceExtension extends Extension {
		default ExtensionStatus afterCreateResource(String method, CreateResourceRequest createResourceRequest, CreateResourceState createResourceState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return null;
		}
	}

	@ExtensionStage("afterUpdateResource")
	interface AfterUpdateResourceExtension extends Extension {
		default ExtensionStatus afterUpdateResource(String method, UpdateResourceRequest updateResourceRequest, UpdateResourceState updateResourceState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return null;
		}
	}

	@ExtensionStage("afterDeactivateResource")
	interface AfterDeactivateResourceExtension extends Extension {
		default ExtensionStatus afterDeactivateResource(String method, DeactivateResourceRequest deactivateResourceRequest, DeactivateResourceState deactivateResourceState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
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

	@ExtensionStage("beforeWriteExecute")
	interface BeforeWriteExecuteExtension extends Extension {
		default void beforeWriteExecute(String method, Map<String, Object> stateMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		}
	}

	@ExtensionStage("beforeWriteCreateResource")
	interface BeforeWriteCreateResourceExtension extends Extension {
		default void beforeWriteCreateResource(String method, Map<String, Object> stateMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		}
	}

	@ExtensionStage("beforeWriteUpdateResource")
	interface BeforeWriteUpdateResourceExtension extends Extension {
		default void beforeWriteUpdateResource(String method, Map<String, Object> stateMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		}
	}

	@ExtensionStage("beforeWriteDeactivateResource")
	interface BeforeWriteDeactivateResourceExtension extends Extension {
		default void beforeWriteDeactivateResource(String method, Map<String, Object> stateMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		}
	}

	/*
	 * Helper classes
	 */

	abstract class AbstractExtension implements BeforeReadCreateExtension, BeforeReadUpdateExtension, BeforeReadDeactivateExtension, BeforeReadExecuteExtension, BeforeReadCreateResourceExtension, BeforeReadUpdateResourceExtension, BeforeReadDeactivateResourceExtension, BeforeCreateExtension, BeforeUpdateExtension, BeforeDeactivateExtension, BeforeExecuteExtension, BeforeCreateResourceExtension, BeforeUpdateResourceExtension, BeforeDeactivateResourceExtension, BeforeDriverWriteCreateExtension, BeforeDriverWriteUpdateExtension, BeforeDriverWriteDeactivateExtension, BeforeDriverWriteExecuteExtension, BeforeDriverWriteCreateResourceExtension, BeforeDriverWriteUpdateResourceExtension, BeforeDriverWriteDeactivateResourceExtension, BeforeDriverReadCreateResourceExtension, BeforeDriverReadCreateExtension, BeforeDriverReadUpdateExtension, BeforeDriverReadDeactivateExtension, BeforeDriverReadExecuteExtension, BeforeDriverReadUpdateResourceExtension, BeforeDriverReadDeactivateResourceExtension, AfterCreateExtension, AfterUpdateExtension, AfterDeactivateExtension, AfterExecuteExtension, AfterCreateResourceExtension, AfterUpdateResourceExtension, AfterDeactivateResourceExtension, BeforeWriteCreateExtension, BeforeWriteUpdateExtension, BeforeWriteDeactivateExtension, BeforeWriteExecuteExtension, BeforeWriteCreateResourceExtension, BeforeWriteUpdateResourceExtension, BeforeWriteDeactivateResourceExtension {
	}

	abstract class AbstractBeforeReadExtension implements Extension.BeforeReadCreateExtension, Extension.BeforeReadUpdateExtension, Extension.BeforeReadDeactivateExtension, Extension.BeforeReadExecuteExtension, Extension.BeforeReadCreateResourceExtension, Extension.BeforeReadUpdateResourceExtension, Extension.BeforeReadDeactivateResourceExtension {

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

		@Override
		public final void beforeReadExecute(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			this.beforeRead(method, requestMap, localUniRegistrar);
		}

		@Override
		public final void beforeReadCreateResource(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			this.beforeReadResource(method, requestMap, localUniRegistrar);
		}

		@Override
		public final void beforeReadUpdateResource(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			this.beforeReadResource(method, requestMap, localUniRegistrar);
		}

		@Override
		public final void beforeReadDeactivateResource(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			this.beforeReadResource(method, requestMap, localUniRegistrar);
		}

		public void beforeRead(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			this.before(method, requestMap, localUniRegistrar);
		}
		public void beforeReadResource(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			this.before(method, requestMap, localUniRegistrar);
		}
		public void before(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		}
	}

	abstract class AbstractBeforeRequestExtension implements Extension.BeforeCreateExtension, Extension.BeforeUpdateExtension, Extension.BeforeDeactivateExtension, Extension.BeforeExecuteExtension, Extension.BeforeCreateResourceExtension, Extension.BeforeUpdateResourceExtension, Extension.BeforeDeactivateResourceExtension {

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
		public final ExtensionStatus beforeExecute(String method, ExecuteRequest executeRequest, ExecuteState executeState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.beforeRequest(method, executeRequest, executeState, executionState, localUniRegistrar);
		}

		@Override
		public final ExtensionStatus beforeCreateResource(String method, CreateResourceRequest createResourceRequest, CreateResourceState createResourceState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.beforeRequestResource(method, createResourceRequest, createResourceState, executionState, localUniRegistrar);
		}

		@Override
		public final ExtensionStatus beforeUpdateResource(String method, UpdateResourceRequest updateResourceRequest, UpdateResourceState updateResourceState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.beforeRequestResource(method, updateResourceRequest, updateResourceState, executionState, localUniRegistrar);
		}

		@Override
		public final ExtensionStatus beforeDeactivateResource(String method, DeactivateResourceRequest deactivateResourceRequest, DeactivateResourceState deactivateResourceState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.beforeRequestResource(method, deactivateResourceRequest, deactivateResourceState, executionState, localUniRegistrar);
		}

		public ExtensionStatus beforeRequest(String method, RegistrarRequest request, RegistrarState state, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.before(method, request, executionState, localUniRegistrar);
		}
		public ExtensionStatus beforeRequestResource(String method, RegistrarRequest request, RegistrarResourceState state, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.before(method, request, executionState, localUniRegistrar);
		}
		public ExtensionStatus before(String method, RegistrarRequest request, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return null;
		}
	}

	abstract class AbstractBeforeDriverWriteExtension implements Extension.BeforeDriverWriteCreateExtension, Extension.BeforeDriverWriteUpdateExtension, Extension.BeforeDriverWriteDeactivateExtension, Extension.BeforeDriverWriteExecuteExtension, Extension.BeforeDriverWriteCreateResourceExtension, Extension.BeforeDriverWriteUpdateResourceExtension, Extension.BeforeDriverWriteDeactivateResourceExtension {

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

		@Override
		public final void beforeDriverWriteExecute(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			this.beforeDriverWrite(method, requestMap, localUniRegistrar);
		}

		@Override
		public final void beforeDriverWriteCreateResource(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			this.beforeDriverWriteResource(method, requestMap, localUniRegistrar);
		}

		@Override
		public final void beforeDriverWriteUpdateResource(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			this.beforeDriverWriteResource(method, requestMap, localUniRegistrar);
		}

		@Override
		public final void beforeDriverWriteDeactivateResource(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			this.beforeDriverWriteResource(method, requestMap, localUniRegistrar);
		}

		public void beforeDriverWrite(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			this.before(method, requestMap, localUniRegistrar);
		}
		public void beforeDriverWriteResource(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			this.before(method, requestMap, localUniRegistrar);
		}
		public void before(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		}
	}

	abstract class AbstractBeforeDriverReadExtension implements Extension.BeforeDriverReadCreateExtension, Extension.BeforeDriverReadUpdateExtension, Extension.BeforeDriverReadDeactivateExtension, Extension.BeforeDriverReadExecuteExtension, Extension.BeforeDriverReadCreateResourceExtension, Extension.BeforeDriverReadUpdateResourceExtension, Extension.BeforeDriverReadDeactivateResourceExtension {

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

		@Override
		public final void beforeDriverReadExecute(String method, Map<String, Object> stateMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			this.beforeDriverRead(method, stateMap, localUniRegistrar);
		}

		@Override
		public final void beforeDriverReadCreateResource(String method, Map<String, Object> stateMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			this.beforeDriverReadResource(method, stateMap, localUniRegistrar);
		}

		@Override
		public final void beforeDriverReadUpdateResource(String method, Map<String, Object> stateMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			this.beforeDriverReadResource(method, stateMap, localUniRegistrar);
		}

		@Override
		public final void beforeDriverReadDeactivateResource(String method, Map<String, Object> stateMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			this.beforeDriverReadResource(method, stateMap, localUniRegistrar);
		}

		public void beforeDriverRead(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			this.before(method, requestMap, localUniRegistrar);
		}
		public void beforeDriverReadResource(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			this.before(method, requestMap, localUniRegistrar);
		}
		public void before(String method, Map<String, Object> requestMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		}
	}

	abstract class AbstractAfterRequestExtension implements Extension.AfterCreateExtension, Extension.AfterUpdateExtension, Extension.AfterDeactivateExtension, Extension.AfterExecuteExtension, Extension.AfterCreateResourceExtension, Extension.AfterUpdateResourceExtension, Extension.AfterDeactivateResourceExtension {

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

		@Override
		public final ExtensionStatus afterExecute(String method, ExecuteRequest executeRequest, ExecuteState executeState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.afterRequest(method, executeRequest, executeState, executionState, localUniRegistrar);
		}

		@Override
		public final ExtensionStatus afterCreateResource(String method, CreateResourceRequest createResourceRequest, CreateResourceState createResourceState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.afterRequestResource(method, createResourceRequest, createResourceState, executionState, localUniRegistrar);
		}

		@Override
		public final ExtensionStatus afterUpdateResource(String method, UpdateResourceRequest updateResourceRequest, UpdateResourceState updateResourceState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.afterRequestResource(method, updateResourceRequest, updateResourceState, executionState, localUniRegistrar);
		}

		@Override
		public final ExtensionStatus afterDeactivateResource(String method, DeactivateResourceRequest deactivateResourceRequest, DeactivateResourceState deactivateResourceState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.afterRequestResource(method, deactivateResourceRequest, deactivateResourceState, executionState, localUniRegistrar);
		}

		public ExtensionStatus afterRequest(String method, RegistrarRequest request, RegistrarState state, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.after(method, request, executionState, localUniRegistrar);
		}
		public ExtensionStatus afterRequestResource(String method, RegistrarRequest request, RegistrarResourceState state, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.after(method, request, executionState, localUniRegistrar);
		}
		public ExtensionStatus after(String method, RegistrarRequest request, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return null;
		}
	}

	abstract class AbstractBeforeWriteExtension implements Extension.BeforeWriteCreateExtension, Extension.BeforeWriteUpdateExtension, Extension.BeforeWriteDeactivateExtension, Extension.BeforeWriteExecuteExtension, Extension.BeforeWriteCreateResourceExtension, Extension.BeforeWriteUpdateResourceExtension, Extension.BeforeWriteDeactivateResourceExtension {

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

		@Override
		public final void beforeWriteExecute(String method, Map<String, Object> stateMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			this.beforeWrite(method, stateMap, localUniRegistrar);
		}

		@Override
		public final void beforeWriteCreateResource(String method, Map<String, Object> stateMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			this.beforeWriteResource(method, stateMap, localUniRegistrar);
		}

		@Override
		public final void beforeWriteUpdateResource(String method, Map<String, Object> stateMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			this.beforeWriteResource(method, stateMap, localUniRegistrar);
		}

		@Override
		public final void beforeWriteDeactivateResource(String method, Map<String, Object> stateMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			this.beforeWriteResource(method, stateMap, localUniRegistrar);
		}

		public void beforeWrite(String method, Map<String, Object> stateMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			this.before(method, stateMap, localUniRegistrar);
		}
		public void beforeWriteResource(String method, Map<String, Object> stateMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			this.before(method, stateMap, localUniRegistrar);
		}
		public void before(String method, Map<String, Object> stateMap, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
		}
	}

	abstract class AbstractRequestExtension implements Extension.BeforeCreateExtension, Extension.BeforeUpdateExtension, Extension.BeforeDeactivateExtension, Extension.BeforeExecuteExtension, Extension.BeforeCreateResourceExtension, Extension.BeforeUpdateResourceExtension, Extension.BeforeDeactivateResourceExtension, Extension.AfterCreateExtension, Extension.AfterUpdateExtension, Extension.AfterDeactivateExtension, Extension.AfterExecuteExtension, Extension.AfterCreateResourceExtension, Extension.AfterUpdateResourceExtension, Extension.AfterDeactivateResourceExtension {

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
		public final ExtensionStatus beforeExecute(String method, ExecuteRequest executeRequest, ExecuteState executeState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.beforeRequest(method, executeRequest, executeState, executionState, localUniRegistrar);
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

		@Override
		public final ExtensionStatus afterExecute(String method, ExecuteRequest executeRequest, ExecuteState executeState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.afterRequest(method, executeRequest, executeState, executionState, localUniRegistrar);
		}

		@Override
		public final ExtensionStatus beforeCreateResource(String method, CreateResourceRequest createResourceRequest, CreateResourceState createResourceState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.beforeRequestResource(method, createResourceRequest, createResourceState, executionState, localUniRegistrar);
		}

		@Override
		public final ExtensionStatus beforeUpdateResource(String method, UpdateResourceRequest updateResourceRequest, UpdateResourceState updateResourceState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.beforeRequestResource(method, updateResourceRequest, updateResourceState, executionState, localUniRegistrar);
		}

		@Override
		public final ExtensionStatus beforeDeactivateResource(String method, DeactivateResourceRequest deactivateResourceRequest, DeactivateResourceState deactivateResourceState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.beforeRequestResource(method, deactivateResourceRequest, deactivateResourceState, executionState, localUniRegistrar);
		}

		@Override
		public final ExtensionStatus afterCreateResource(String method, CreateResourceRequest createResourceRequest, CreateResourceState createResourceState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.afterRequestResource(method, createResourceRequest, createResourceState, executionState, localUniRegistrar);
		}

		@Override
		public final ExtensionStatus afterUpdateResource(String method, UpdateResourceRequest updateResourceRequest, UpdateResourceState updateResourceState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.afterRequestResource(method, updateResourceRequest, updateResourceState, executionState, localUniRegistrar);
		}

		@Override
		public final ExtensionStatus afterDeactivateResource(String method, DeactivateResourceRequest deactivateResourceRequest, DeactivateResourceState deactivateResourceState, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.afterRequestResource(method, deactivateResourceRequest, deactivateResourceState, executionState, localUniRegistrar);
		}

		public ExtensionStatus beforeRequest(String method, RegistrarRequest request, RegistrarState state, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.before(method, request, executionState, localUniRegistrar);
		}
		public ExtensionStatus afterRequest(String method, RegistrarRequest request, RegistrarState state, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.after(method, request, executionState, localUniRegistrar);
		}
		public ExtensionStatus beforeRequestResource(String method, RegistrarRequest request, RegistrarResourceState state, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.before(method, request, executionState, localUniRegistrar);
		}
		public ExtensionStatus afterRequestResource(String method, RegistrarRequest request, RegistrarResourceState state, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return this.after(method, request, executionState, localUniRegistrar);
		}
		public ExtensionStatus before(String method, RegistrarRequest request, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return null;
		}
		public ExtensionStatus after(String method, RegistrarRequest request, Map<String, Object> executionState, LocalUniRegistrar localUniRegistrar) throws RegistrationException {
			return null;
		}
	}

	static List<String> extensionClassNames(List<? extends Extension> extensions) {
		return extensions.stream().map(e -> e.getClass().getSimpleName()).toList();
	}
}
