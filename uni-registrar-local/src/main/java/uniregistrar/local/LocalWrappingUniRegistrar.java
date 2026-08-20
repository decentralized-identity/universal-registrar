package uniregistrar.local;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniregistrar.ExecutionStateUniRegistrar;
import uniregistrar.RegistrationException;
import uniregistrar.UniRegistrar;
import uniregistrar.local.extensions.Extension;
import uniregistrar.local.extensions.ExtensionStatus;
import uniregistrar.local.extensions.util.ExecutionStateUtil;
import uniregistrar.openapi.model.*;
import uniregistrar.util.HttpBindingUtil;

import java.util.*;
import java.util.function.Supplier;

public class LocalWrappingUniRegistrar implements UniRegistrar, ExecutionStateUniRegistrar {

	private static final Logger log = LoggerFactory.getLogger(LocalWrappingUniRegistrar.class);

	private UniRegistrar uniRegistrar;
	private List<Extension> extensions = new ArrayList<>();

	public LocalWrappingUniRegistrar() {

	}

	public LocalWrappingUniRegistrar(UniRegistrar uniRegistrar) {
		this.uniRegistrar = uniRegistrar;
	}

	/*
	 * Registrar methods
	 */

	@Override
	public CreateState create(String method, CreateRequest createRequest) throws RegistrationException {

		return this.create(method, createRequest, null);
	}

	@Override
	public CreateState create(String method, CreateRequest createRequest, Map<String, Object> initialExecutionState) throws RegistrationException {

		if (method == null) throw new NullPointerException();
		if (createRequest == null) throw new NullPointerException();

		if (this.getUniRegistrar() == null) throw new RegistrationException("No Universal Registrar configured.");

		// start time

		long start = System.currentTimeMillis();

		// prepare execution state

		Map<String, Object> executionState = new HashMap<>();
		if (initialExecutionState != null) executionState.putAll(initialExecutionState);

		// prepare create state

		CreateState createState = new CreateState();
		createState.setDidRegistrationMetadata(new LinkedHashMap<>());
		createState.setDidDocumentMetadata(new LinkedHashMap<>());
		ExtensionStatus extensionStatus = new ExtensionStatus();

		// [before create]

		this.executeExtensions(Extension.BeforeCreateExtension.class, extensionStatus, e -> e.beforeCreate(method, createRequest, createState, executionState, this), () -> HttpBindingUtil.toHttpBodyRequest(createRequest), () -> HttpBindingUtil.toHttpBodyState(createState), executionState);

		// [create]

		if (! extensionStatus.skipDriver()) {

			UniRegistrar uniRegistrar = this.getUniRegistrar();
			if (log.isInfoEnabled()) log.info("Executing create with request " + createRequest + " with Universal Registrar " + uniRegistrar.getClass().getSimpleName());

			CreateState uniRegistrarCreateState = uniRegistrar.create(method, createRequest);
			if (uniRegistrarCreateState != null) {
				createState.setJobId(uniRegistrarCreateState.getJobId());
				createState.setDidState(uniRegistrarCreateState.getDidState());
				if (uniRegistrarCreateState.getDidRegistrationMetadata() != null) createState.getDidRegistrationMetadata().putAll(uniRegistrarCreateState.getDidRegistrationMetadata());
				if (uniRegistrarCreateState.getDidDocumentMetadata() != null) createState.getDidDocumentMetadata().putAll(uniRegistrarCreateState.getDidDocumentMetadata());
			}

			if (log.isInfoEnabled()) log.info("Executed create with state " + createState + " with Universal Registrar " + uniRegistrar.getClass().getSimpleName());
		}

		// [after create]

		this.executeExtensions(Extension.AfterCreateExtension.class, extensionStatus, e -> e.afterCreate(method, createRequest, createState, executionState, this), () -> HttpBindingUtil.toHttpBodyRequest(createRequest), () -> HttpBindingUtil.toHttpBodyState(createState), executionState);

		// additional metadata

		long stop = System.currentTimeMillis();
		createState.getDidRegistrationMetadata().put("duration", stop - start);
		createState.getDidRegistrationMetadata().put("method", method);

		// done

		return createState;
	}

	@Override
	public UpdateState update(String method, UpdateRequest updateRequest) throws RegistrationException {

		return this.update(method, updateRequest, null);
	}

	@Override
	public UpdateState update(String method, UpdateRequest updateRequest, Map<String, Object> initialExecutionState) throws RegistrationException {

		if (method == null) throw new NullPointerException();
		if (updateRequest == null) throw new NullPointerException();

		if (this.getUniRegistrar() == null) throw new RegistrationException("No Universal Registrar configured.");

		// start time

		long start = System.currentTimeMillis();

		// prepare execution state

		Map<String, Object> executionState = new HashMap<>();
		if (initialExecutionState != null) executionState.putAll(initialExecutionState);

		// prepare update state

		UpdateState updateState = new UpdateState();
		updateState.setDidRegistrationMetadata(new LinkedHashMap<>());
		updateState.setDidDocumentMetadata(new LinkedHashMap<>());
		ExtensionStatus extensionStatus = new ExtensionStatus();

		// [before update]

		this.executeExtensions(Extension.BeforeUpdateExtension.class, extensionStatus, e -> e.beforeUpdate(method, updateRequest, updateState, executionState, this), () -> HttpBindingUtil.toHttpBodyRequest(updateRequest), () -> HttpBindingUtil.toHttpBodyState(updateState), executionState);

		// [update]

		if (! extensionStatus.skipDriver()) {

			UniRegistrar uniRegistrar = this.getUniRegistrar();
			if (log.isInfoEnabled()) log.info("Executing update with request " + updateRequest + " with Universal Registrar " + uniRegistrar.getClass().getSimpleName());

			UpdateState uniRegistrarUpdateState = uniRegistrar.update(method, updateRequest);
			if (uniRegistrarUpdateState != null) {
				updateState.setJobId(uniRegistrarUpdateState.getJobId());
				updateState.setDidState(uniRegistrarUpdateState.getDidState());
				if (uniRegistrarUpdateState.getDidRegistrationMetadata() != null) updateState.getDidRegistrationMetadata().putAll(uniRegistrarUpdateState.getDidRegistrationMetadata());
				if (uniRegistrarUpdateState.getDidDocumentMetadata() != null) updateState.getDidDocumentMetadata().putAll(uniRegistrarUpdateState.getDidDocumentMetadata());
			}

			if (log.isInfoEnabled()) log.info("Executed update with state " + updateState + " with Universal Registrar " + uniRegistrar.getClass().getSimpleName());
		}

		// [after update]

		this.executeExtensions(Extension.AfterUpdateExtension.class, extensionStatus, e -> e.afterUpdate(method, updateRequest, updateState, executionState, this), () -> HttpBindingUtil.toHttpBodyRequest(updateRequest), () -> HttpBindingUtil.toHttpBodyState(updateState), executionState);

		// additional metadata

		long stop = System.currentTimeMillis();
		updateState.getDidRegistrationMetadata().put("duration", stop - start);
		updateState.getDidRegistrationMetadata().put("method", method);

		// done

		return updateState;
	}

	@Override
	public DeactivateState deactivate(String method, DeactivateRequest deactivateRequest) throws RegistrationException {

		return this.deactivate(method, deactivateRequest, null);
	}

	@Override
	public DeactivateState deactivate(String method, DeactivateRequest deactivateRequest, Map<String, Object> initialExecutionState) throws RegistrationException {

		if (method == null) throw new NullPointerException();
		if (deactivateRequest == null) throw new NullPointerException();

		if (this.getUniRegistrar() == null) throw new RegistrationException("No Universal Registrar configured.");

		// start time

		long start = System.currentTimeMillis();

		// prepare execution state

		Map<String, Object> executionState = new HashMap<>();
		if (initialExecutionState != null) executionState.putAll(initialExecutionState);

		// prepare deactivate state

		DeactivateState deactivateState = new DeactivateState();
		deactivateState.setDidRegistrationMetadata(new LinkedHashMap<>());
		deactivateState.setDidDocumentMetadata(new LinkedHashMap<>());
		ExtensionStatus extensionStatus = new ExtensionStatus();

		// [before deactivate]

		this.executeExtensions(Extension.BeforeDeactivateExtension.class, extensionStatus, e -> e.beforeDeactivate(method, deactivateRequest, deactivateState, executionState, this), () -> HttpBindingUtil.toHttpBodyRequest(deactivateRequest), () -> HttpBindingUtil.toHttpBodyState(deactivateState), executionState);

		// [deactivate]

		if (! extensionStatus.skipDriver()) {

			UniRegistrar uniRegistrar = this.getUniRegistrar();
			if (log.isInfoEnabled()) log.info("Executing deactivate with request " + deactivateRequest + " with Universal Registrar " + uniRegistrar.getClass().getSimpleName());

			DeactivateState uniRegistrarDeactivateState = uniRegistrar.deactivate(method, deactivateRequest);
			if (uniRegistrarDeactivateState != null) {
				deactivateState.setJobId(uniRegistrarDeactivateState.getJobId());
				deactivateState.setDidState(uniRegistrarDeactivateState.getDidState());
				if (uniRegistrarDeactivateState.getDidRegistrationMetadata() != null) deactivateState.getDidRegistrationMetadata().putAll(uniRegistrarDeactivateState.getDidRegistrationMetadata());
				if (uniRegistrarDeactivateState.getDidDocumentMetadata() != null) deactivateState.getDidDocumentMetadata().putAll(uniRegistrarDeactivateState.getDidDocumentMetadata());
			}

			if (log.isInfoEnabled()) log.info("Executed deactivate with state " + deactivateState + " with Universal Registrar " + uniRegistrar.getClass().getSimpleName());
		}

		// [after deactivate]

		this.executeExtensions(Extension.AfterDeactivateExtension.class, extensionStatus, e -> e.afterDeactivate(method, deactivateRequest, deactivateState, executionState, this), () -> HttpBindingUtil.toHttpBodyRequest(deactivateRequest), () -> HttpBindingUtil.toHttpBodyState(deactivateState), executionState);

		// additional metadata

		long stop = System.currentTimeMillis();
		deactivateState.getDidRegistrationMetadata().put("duration", stop - start);
		deactivateState.getDidRegistrationMetadata().put("method", method);

		// done

		return deactivateState;
	}

	@Override
	public ExecuteState execute(String method, ExecuteRequest executeRequest) throws RegistrationException {

		return this.execute(method, executeRequest, null);
	}

	@Override
	public ExecuteState execute(String method, ExecuteRequest executeRequest, Map<String, Object> initialExecutionState) throws RegistrationException {

		if (method == null) throw new NullPointerException();
		if (executeRequest == null) throw new NullPointerException();

		if (this.getUniRegistrar() == null) throw new RegistrationException("No Universal Registrar configured.");

		// start time

		long start = System.currentTimeMillis();

		// prepare execution state

		Map<String, Object> executionState = new HashMap<>();
		if (initialExecutionState != null) executionState.putAll(initialExecutionState);

		// prepare execute state

		ExecuteState executeState = new ExecuteState();
		executeState.setDidRegistrationMetadata(new LinkedHashMap<>());
		executeState.setDidDocumentMetadata(new LinkedHashMap<>());
		ExtensionStatus extensionStatus = new ExtensionStatus();

		// [before execute]

		this.executeExtensions(Extension.BeforeExecuteExtension.class, extensionStatus, e -> e.beforeExecute(method, executeRequest, executeState, executionState, this), () -> HttpBindingUtil.toHttpBodyRequest(executeRequest), () -> HttpBindingUtil.toHttpBodyState(executeState), executionState);

		// [execute]

		if (! extensionStatus.skipDriver()) {

			UniRegistrar uniRegistrar = this.getUniRegistrar();
			if (log.isInfoEnabled()) log.info("Executing update with request " + executeRequest + " with Universal Registrar " + uniRegistrar.getClass().getSimpleName());

			ExecuteState uniRegistrarExecuteState = uniRegistrar.execute(method, executeRequest);
			if (uniRegistrarExecuteState != null) {
				executeState.setJobId(uniRegistrarExecuteState.getJobId());
				executeState.setDidState(uniRegistrarExecuteState.getDidState());
				if (uniRegistrarExecuteState.getDidRegistrationMetadata() != null) executeState.getDidRegistrationMetadata().putAll(uniRegistrarExecuteState.getDidRegistrationMetadata());
				if (uniRegistrarExecuteState.getDidDocumentMetadata() != null) executeState.getDidDocumentMetadata().putAll(uniRegistrarExecuteState.getDidDocumentMetadata());
				executeState.setOperationResult(uniRegistrarExecuteState.getOperationResult());
			}

			if (log.isInfoEnabled()) log.info("Executed execute with state " + executeState + " with Universal Registrar " + uniRegistrar.getClass().getSimpleName());
		}

		// [after execute]

		this.executeExtensions(Extension.AfterExecuteExtension.class, extensionStatus, e -> e.afterExecute(method, executeRequest, executeState, executionState, this), () -> HttpBindingUtil.toHttpBodyRequest(executeRequest), () -> HttpBindingUtil.toHttpBodyState(executeState), executionState);

		// additional metadata

		long stop = System.currentTimeMillis();
		executeState.getDidRegistrationMetadata().put("duration", stop - start);
		executeState.getDidRegistrationMetadata().put("method", method);

		// done

		return executeState;
	}

	@Override
	public CreateResourceState createResource(String method, CreateResourceRequest createResourceRequest) throws RegistrationException {

		return this.createResource(method, createResourceRequest, null);
	}

	@Override
	public CreateResourceState createResource(String method, CreateResourceRequest createResourceRequest, Map<String, Object> initialExecutionState) throws RegistrationException {

		if (method == null) throw new NullPointerException();
		if (createResourceRequest == null) throw new NullPointerException();

		if (this.getUniRegistrar() == null) throw new RegistrationException("No Universal Registrar configured.");

		// start time

		long start = System.currentTimeMillis();

		// prepare execution state

		Map<String, Object> executionState = new HashMap<>();
		if (initialExecutionState != null) executionState.putAll(initialExecutionState);

		// prepare createResource state

		CreateResourceState createResourceState = new CreateResourceState();
		createResourceState.setDidRegistrationMetadata(new LinkedHashMap<>());
		createResourceState.setContentMetadata(new LinkedHashMap<>());
		ExtensionStatus extensionStatus = new ExtensionStatus();

		// [before createResource]

		this.executeExtensions(Extension.BeforeCreateResourceExtension.class, extensionStatus, e -> e.beforeCreateResource(method, createResourceRequest, createResourceState, executionState, this), () -> HttpBindingUtil.toHttpBodyRequest(createResourceRequest), () -> HttpBindingUtil.toHttpBodyResourceState(createResourceState), executionState);

		// [createResource]

		if (! extensionStatus.skipDriver()) {

			UniRegistrar uniRegistrar = this.getUniRegistrar();
			if (log.isInfoEnabled()) log.info("Executing createResource with request " + createResourceRequest + " with Universal Registrar " + uniRegistrar.getClass().getSimpleName());

			CreateResourceState uniRegistrarCreateResourceState = uniRegistrar.createResource(method, createResourceRequest);
			if (uniRegistrarCreateResourceState != null) {
				createResourceState.setJobId(uniRegistrarCreateResourceState.getJobId());
				createResourceState.setDidUrlState(uniRegistrarCreateResourceState.getDidUrlState());
				if (uniRegistrarCreateResourceState.getDidRegistrationMetadata() != null) createResourceState.getDidRegistrationMetadata().putAll(uniRegistrarCreateResourceState.getDidRegistrationMetadata());
				if (uniRegistrarCreateResourceState.getContentMetadata() != null) createResourceState.getContentMetadata().putAll(uniRegistrarCreateResourceState.getContentMetadata());
			}

			if (log.isInfoEnabled()) log.info("Executed createResource resource with state " + createResourceState + " with Universal Registrar " + uniRegistrar.getClass().getSimpleName());
		}

		// [after createResource]

		this.executeExtensions(Extension.AfterCreateResourceExtension.class, extensionStatus, e -> e.afterCreateResource(method, createResourceRequest, createResourceState, executionState, this), () -> HttpBindingUtil.toHttpBodyRequest(createResourceRequest), () -> HttpBindingUtil.toHttpBodyResourceState(createResourceState), executionState);

		// additional metadata

		long stop = System.currentTimeMillis();
		createResourceState.getDidRegistrationMetadata().put("duration", stop - start);
		createResourceState.getDidRegistrationMetadata().put("method", method);

		// done

		return createResourceState;
	}

	@Override
	public UpdateResourceState updateResource(String method, UpdateResourceRequest updateResourceRequest) throws RegistrationException {

		return this.updateResource(method, updateResourceRequest, null);
	}

	@Override
	public UpdateResourceState updateResource(String method, UpdateResourceRequest updateResourceRequest, Map<String, Object> initialExecutionState) throws RegistrationException {

		if (method == null) throw new NullPointerException();
		if (updateResourceRequest == null) throw new NullPointerException();

		if (this.getUniRegistrar() == null) throw new RegistrationException("No Universal Registrar configured.");

		// start time

		long start = System.currentTimeMillis();

		// prepare execution state

		Map<String, Object> executionState = new HashMap<>();
		if (initialExecutionState != null) executionState.putAll(initialExecutionState);

		// prepare updateResource state

		UpdateResourceState updateResourceState = new UpdateResourceState();
		updateResourceState.setDidRegistrationMetadata(new LinkedHashMap<>());
		updateResourceState.setContentMetadata(new LinkedHashMap<>());
		ExtensionStatus extensionStatus = new ExtensionStatus();

		// [before updateResource]

		this.executeExtensions(Extension.BeforeUpdateResourceExtension.class, extensionStatus, e -> e.beforeUpdateResource(method, updateResourceRequest, updateResourceState, executionState, this), () -> HttpBindingUtil.toHttpBodyRequest(updateResourceRequest), () -> HttpBindingUtil.toHttpBodyResourceState(updateResourceState), executionState);

		// [updateResource]

		if (! extensionStatus.skipDriver()) {
			UniRegistrar uniRegistrar = this.getUniRegistrar();
			if (log.isInfoEnabled()) log.info("Executing updateResource with request " + updateResourceRequest + " with Universal Registrar " + uniRegistrar.getClass().getSimpleName());

			UpdateResourceState uniRegistrarUpdateResourceState = uniRegistrar.updateResource(method, updateResourceRequest);
			if (uniRegistrarUpdateResourceState != null) {
				updateResourceState.setJobId(uniRegistrarUpdateResourceState.getJobId());
				updateResourceState.setDidUrlState(uniRegistrarUpdateResourceState.getDidUrlState());
				if (uniRegistrarUpdateResourceState.getDidRegistrationMetadata() != null) updateResourceState.getDidRegistrationMetadata().putAll(uniRegistrarUpdateResourceState.getDidRegistrationMetadata());
				if (uniRegistrarUpdateResourceState.getContentMetadata() != null) updateResourceState.getContentMetadata().putAll(uniRegistrarUpdateResourceState.getContentMetadata());
			}

			if (log.isInfoEnabled()) log.info("Executed updateResource with state " + updateResourceState + " with Universal Registrar " + uniRegistrar.getClass().getSimpleName());
		}

		// [after updateResource]

		this.executeExtensions(Extension.AfterUpdateResourceExtension.class, extensionStatus, e -> e.afterUpdateResource(method, updateResourceRequest, updateResourceState, executionState, this), () -> HttpBindingUtil.toHttpBodyRequest(updateResourceRequest), () -> HttpBindingUtil.toHttpBodyResourceState(updateResourceState), executionState);

		// additional metadata

		long stop = System.currentTimeMillis();
		updateResourceState.getDidRegistrationMetadata().put("duration", stop - start);
		updateResourceState.getDidRegistrationMetadata().put("method", method);

		// done

		return updateResourceState;
	}

	@Override
	public DeactivateResourceState deactivateResource(String method, DeactivateResourceRequest deactivateResourceRequest) throws RegistrationException {

		return this.deactivateResource(method, deactivateResourceRequest, null);
	}

	@Override
	public DeactivateResourceState deactivateResource(String method, DeactivateResourceRequest deactivateResourceRequest, Map<String, Object> initialExecutionState) throws RegistrationException {

		if (method == null) throw new NullPointerException();
		if (deactivateResourceRequest == null) throw new NullPointerException();

		if (this.getUniRegistrar() == null) throw new RegistrationException("No Universal Registrar configured.");

		// start time

		long start = System.currentTimeMillis();

		// prepare execution state

		Map<String, Object> executionState = new HashMap<>();
		if (initialExecutionState != null) executionState.putAll(initialExecutionState);

		// prepare deactivateResource state

		DeactivateResourceState deactivateResourceState = new DeactivateResourceState();
		deactivateResourceState.setDidRegistrationMetadata(new LinkedHashMap<>());
		deactivateResourceState.setContentMetadata(new LinkedHashMap<>());
		ExtensionStatus extensionStatus = new ExtensionStatus();

		// [before deactivateResource]

		this.executeExtensions(Extension.BeforeDeactivateResourceExtension.class, extensionStatus, e -> e.beforeDeactivateResource(method, deactivateResourceRequest, deactivateResourceState, executionState, this), () -> HttpBindingUtil.toHttpBodyRequest(deactivateResourceRequest), () -> HttpBindingUtil.toHttpBodyResourceState(deactivateResourceState), executionState);

		// [deactivateResource]

		if (! extensionStatus.skipDriver()) {
			UniRegistrar uniRegistrar = this.getUniRegistrar();
			if (log.isInfoEnabled()) log.info("Executing deactivateResource with request " + deactivateResourceRequest + " with Universal Registrar " + uniRegistrar.getClass().getSimpleName());

			DeactivateResourceState uniRegistrarDeactivateState = uniRegistrar.deactivateResource(method, deactivateResourceRequest);
			if (uniRegistrarDeactivateState != null) {
				deactivateResourceState.setJobId(uniRegistrarDeactivateState.getJobId());
				deactivateResourceState.setDidUrlState(uniRegistrarDeactivateState.getDidUrlState());
				if (uniRegistrarDeactivateState.getDidRegistrationMetadata() != null) deactivateResourceState.getDidRegistrationMetadata().putAll(uniRegistrarDeactivateState.getDidRegistrationMetadata());
				if (uniRegistrarDeactivateState.getContentMetadata() != null) deactivateResourceState.getContentMetadata().putAll(uniRegistrarDeactivateState.getContentMetadata());
			}

			if (log.isInfoEnabled()) log.info("Executed deactivateResource with state " + deactivateResourceState + " with Universal Registrar " + uniRegistrar.getClass().getSimpleName());
		}

		// [after deactivateResource]

		this.executeExtensions(Extension.AfterDeactivateResourceExtension.class, extensionStatus, e -> e.afterDeactivateResource(method, deactivateResourceRequest, deactivateResourceState, executionState, this), () -> HttpBindingUtil.toHttpBodyRequest(deactivateResourceRequest), () -> HttpBindingUtil.toHttpBodyResourceState(deactivateResourceState), executionState);

		// additional metadata

		long stop = System.currentTimeMillis();
		deactivateResourceState.getDidRegistrationMetadata().put("duration", stop - start);
		deactivateResourceState.getDidRegistrationMetadata().put("method", method);

		// done

		return deactivateResourceState;
	}

	public <E extends Extension> void executeExtensions(Class<E> extensionClass, ExtensionStatus extensionStatus, Extension.ExtensionFunction<E> extensionFunction, Supplier<String> requestSupplier, Supplier<String> stateSupplier, Map<String, Object> executionState) throws RegistrationException {

		String extensionStage = extensionClass.getAnnotation(Extension.ExtensionStage.class).value();

		List<E> extensions = this.getExtensions().stream().filter(extensionClass::isInstance).map(extensionClass::cast).toList();
		if (log.isDebugEnabled()) log.debug("EXTENSIONS (" + extensionStage + "), TRYING: {}", Extension.extensionClassNames(extensions));

		List<Extension> skippedExtensions = new ArrayList<>();
		List<Extension> inapplicableExtensions = new ArrayList<>();

		for (E extension : extensions) {
			if (extensionStatus.skip(extensionStage)) { skippedExtensions.add(extension); continue; }
			ExtensionStatus returnedExtensionStatus = extensionFunction.apply(extension);
			extensionStatus.or(returnedExtensionStatus);
			if (returnedExtensionStatus == null) { inapplicableExtensions.add(extension); continue; }
			if (log.isDebugEnabled()) log.debug("Executed extension (" + extensionStage + ") " + extension.getClass().getSimpleName() + "\n-->REQUEST: " + requestSupplier.get() + "\n-->STATE: " + stateSupplier.get() + "\n-->EXECUTION STATE: " + executionState);
			ExecutionStateUtil.addExtensionStage(executionState, extensionClass, extension);
		}

		if (log.isDebugEnabled()) {
			List<E> executedExtensions = extensions.stream().filter(e -> ! skippedExtensions.contains(e)).filter(e -> ! inapplicableExtensions.contains(e)).toList();
			log.debug("EXTENSIONS (" + extensionStage + "), EXECUTED: {}, SKIPPED: {}, INAPPLICABLE: {}", Extension.extensionClassNames(executedExtensions), Extension.extensionClassNames(skippedExtensions), Extension.extensionClassNames(inapplicableExtensions));
		}
	}

	public <E extends Extension> void executeExtensions(Class<E> extensionClass, Extension.ExtensionFunctionVoid<E> extensionFunction, Map<String, Object> map) throws RegistrationException {

		String extensionStage = extensionClass.getAnnotation(Extension.ExtensionStage.class).value();

		List<E> extensions = this.getExtensions().stream().filter(extensionClass::isInstance).map(extensionClass::cast).toList();
		if (log.isDebugEnabled()) log.debug("For extension stage '" + extensionStage + "' trying the following extensions: " + Extension.extensionClassNames(extensions));

		for (E extension : extensions) {
			extensionFunction.apply(extension);
			if (log.isDebugEnabled()) log.debug("Executed extension (" + extensionStage + ") " + extension.getClass().getSimpleName() + "\n-->MAP: " + HttpBindingUtil.toHttpBodyMap(map));
		}
	}

	@Override
	public Map<String, Map<String, Object>> properties() throws RegistrationException {
		if (this.getUniRegistrar() == null) throw new RegistrationException("No Universal Registrar configured.");
		return this.getUniRegistrar().properties();
	}

	@Override
	public Set<String> methods() throws RegistrationException {
		if (this.getUniRegistrar() == null) throw new RegistrationException("No Universal Registrar configured.");
		return this.getUniRegistrar().methods();
	}

	@Override
	public Map<String, Map<String, Object>> traits() throws RegistrationException {
		if (this.getUniRegistrar() == null) throw new RegistrationException("No Universal Registrar configured.");
		return this.getUniRegistrar().traits();
	}

	/*
	 * Getters and setters
	 */

	public UniRegistrar getUniRegistrar() {
		return uniRegistrar;
	}

	public void setUniRegistrar(UniRegistrar uniRegistrar) {
		this.uniRegistrar = uniRegistrar;
	}

	public List<Extension> getExtensions() {
		return this.extensions;
	}

	public void setExtensions(List<Extension> extensions) {
		this.extensions = extensions;
	}
}
