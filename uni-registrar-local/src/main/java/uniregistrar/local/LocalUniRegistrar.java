package uniregistrar.local;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniregistrar.RegistrationException;
import uniregistrar.UniRegistrar;
import uniregistrar.driver.Driver;
import uniregistrar.driver.http.HttpDriver;
import uniregistrar.local.configuration.LocalUniRegistrarConfigurator;
import uniregistrar.local.extensions.Extension;
import uniregistrar.local.extensions.ExtensionStatus;
import uniregistrar.local.extensions.util.ExecutionStateUtil;
import uniregistrar.openapi.model.*;
import uniregistrar.util.HttpBindingUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LocalUniRegistrar implements UniRegistrar {

	private static final Logger log = LoggerFactory.getLogger(LocalUniRegistrar.class);

	private Map<String, Driver> drivers = new LinkedHashMap<>();
	private List<Extension> extensions = new ArrayList<>();

	public LocalUniRegistrar() {

	}

	public LocalUniRegistrar(Map<String, Driver> drivers) {
		this.drivers = drivers;
	}

	/*
	 * Factory methods
	 */

	public static LocalUniRegistrar fromConfigFile(String filePath) throws FileNotFoundException, IOException {

		LocalUniRegistrar localUniRegistrar = new LocalUniRegistrar();
		LocalUniRegistrarConfigurator.configureLocalUniRegistrar(filePath, localUniRegistrar);

		return localUniRegistrar;
	}

	@Override
	public CreateState create(String method, CreateRequest createRequest) throws RegistrationException {

		return this.create(method, createRequest, null);
	}

	public CreateState create(String method, CreateRequest createRequest, Map<String, Object> initialExecutionState) throws RegistrationException {

		if (method == null) throw new NullPointerException();
		if (createRequest == null) throw new NullPointerException();

		if (this.getDrivers() == null) throw new RegistrationException("No drivers configured.");

		// start time

		long start = System.currentTimeMillis();

		// prepare execution state

		Map<String, Object> executionState = new HashMap<>();
		if (initialExecutionState != null) executionState.putAll(initialExecutionState);

		// prepare create state

		CreateState createState = new CreateState();
		ExtensionStatus extensionStatus = new ExtensionStatus();

		// [before create]

		this.executeExtensions(Extension.BeforeCreateExtension.class, extensionStatus, e -> e.beforeCreate(method, createRequest, createState, executionState, this), () -> HttpBindingUtil.toHttpBodyRequest(createRequest), () -> HttpBindingUtil.toHttpBodyState(createState), executionState);

		// [before driver write create]

		final Consumer<Map<String, Object>> beforeDriverWriteCreateConsumer = requestMap -> {
            try {
                LocalUniRegistrar.this.executeExtensions(Extension.BeforeDriverWriteCreateExtension.class, e -> e.beforeDriverWriteCreate(method, requestMap, LocalUniRegistrar.this), requestMap);
            } catch (RegistrationException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        };

		// [before driver read create]

		final Consumer<Map<String, Object>> beforeDriverReadCreateConsumer = stateMap -> {
            try {
                LocalUniRegistrar.this.executeExtensions(Extension.BeforeDriverReadCreateExtension.class, e -> e.beforeDriverReadCreate(method, stateMap, LocalUniRegistrar.this), stateMap);
            } catch (RegistrationException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        };

		// [create]

		if (! extensionStatus.skipDriver()) {

			Driver driver = this.getDrivers().get(method);
			if (driver == null) throw new RegistrationException(RegistrationException.ERROR_BADREQUEST, "Unsupported method: " + method);
			if (log.isInfoEnabled()) log.info("Executing create with request " + createRequest + " with driver " + driver.getClass().getSimpleName());

			if (driver instanceof HttpDriver httpDriver) {
				httpDriver.setBeforeWriteCreateConsumer(beforeDriverWriteCreateConsumer);
				httpDriver.setBeforeReadCreateConsumer(beforeDriverReadCreateConsumer);
			}

			CreateState driverCreateState = driver.create(createRequest);
			if (driverCreateState != null) {
				createState.setJobId(driverCreateState.getJobId());
				createState.setDidState(driverCreateState.getDidState());
				if (driverCreateState.getDidRegistrationMetadata() != null) createState.getDidRegistrationMetadata().putAll(driverCreateState.getDidRegistrationMetadata());
				if (driverCreateState.getDidDocumentMetadata() != null) createState.getDidDocumentMetadata().putAll(driverCreateState.getDidDocumentMetadata());
			}

			if (log.isInfoEnabled()) log.info("Executed create with state " + createState + " with driver " + driver.getClass().getSimpleName());
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

	public UpdateState update(String method, UpdateRequest updateRequest, Map<String, Object> initialExecutionState) throws RegistrationException {

		if (method == null) throw new NullPointerException();
		if (updateRequest == null) throw new NullPointerException();

		if (this.getDrivers() == null) throw new RegistrationException("No drivers configured.");

		// start time

		long start = System.currentTimeMillis();

		// prepare execution state

		Map<String, Object> executionState = new HashMap<>();
		if (initialExecutionState != null) executionState.putAll(initialExecutionState);

		// prepare update state

		UpdateState updateState = new UpdateState();
		ExtensionStatus extensionStatus = new ExtensionStatus();

		// [before update]

		this.executeExtensions(Extension.BeforeUpdateExtension.class, extensionStatus, e -> e.beforeUpdate(method, updateRequest, updateState, executionState, this), () -> HttpBindingUtil.toHttpBodyRequest(updateRequest), () -> HttpBindingUtil.toHttpBodyState(updateState), executionState);

		// [before driver write update]

		final Consumer<Map<String, Object>> beforeDriverWriteUpdateConsumer = requestMap -> {
			try {
				LocalUniRegistrar.this.executeExtensions(Extension.BeforeDriverWriteUpdateExtension.class, e -> e.beforeDriverWriteUpdate(method, requestMap, LocalUniRegistrar.this), requestMap);
			} catch (RegistrationException ex) {
				throw new RuntimeException(ex.getMessage(), ex);
			}
		};

		// [before driver read update]

		final Consumer<Map<String, Object>> beforeDriverReadUpdateConsumer = stateMap -> {
			try {
				LocalUniRegistrar.this.executeExtensions(Extension.BeforeDriverReadUpdateExtension.class, e -> e.beforeDriverReadUpdate(method, stateMap, LocalUniRegistrar.this), stateMap);
			} catch (RegistrationException ex) {
				throw new RuntimeException(ex.getMessage(), ex);
			}
		};

		// [update]

		if (! extensionStatus.skipDriver()) {

			Driver driver = this.getDrivers().get(method);
			if (driver == null) throw new RegistrationException(RegistrationException.ERROR_BADREQUEST, "Unsupported method: " + method);
			if (log.isInfoEnabled()) log.info("Executing update with request " + updateRequest + " with driver " + driver.getClass().getSimpleName());

			if (driver instanceof HttpDriver httpDriver) {
				httpDriver.setBeforeWriteUpdateConsumer(beforeDriverWriteUpdateConsumer);
				httpDriver.setBeforeReadUpdateConsumer(beforeDriverReadUpdateConsumer);
			}

			UpdateState driverUpdateState = driver.update(updateRequest);
			if (driverUpdateState != null) {
				updateState.setJobId(driverUpdateState.getJobId());
				updateState.setDidState(driverUpdateState.getDidState());
				if (driverUpdateState.getDidRegistrationMetadata() != null) updateState.getDidRegistrationMetadata().putAll(driverUpdateState.getDidRegistrationMetadata());
				if (driverUpdateState.getDidDocumentMetadata() != null) updateState.getDidDocumentMetadata().putAll(driverUpdateState.getDidDocumentMetadata());
			}

			if (log.isInfoEnabled()) log.info("Executed update with state " + updateState + " with driver " + driver.getClass().getSimpleName());
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

	public DeactivateState deactivate(String method, DeactivateRequest deactivateRequest, Map<String, Object> initialExecutionState) throws RegistrationException {

		if (method == null) throw new NullPointerException();
		if (deactivateRequest == null) throw new NullPointerException();

		if (this.getDrivers() == null) throw new RegistrationException("No drivers configured.");

		// start time

		long start = System.currentTimeMillis();

		// prepare execution state

		Map<String, Object> executionState = new HashMap<>();
		if (initialExecutionState != null) executionState.putAll(initialExecutionState);

		// prepare deactivate state

		DeactivateState deactivateState = new DeactivateState();
		ExtensionStatus extensionStatus = new ExtensionStatus();

		// [before deactivate]

		this.executeExtensions(Extension.BeforeDeactivateExtension.class, extensionStatus, e -> e.beforeDeactivate(method, deactivateRequest, deactivateState, executionState, this), () -> HttpBindingUtil.toHttpBodyRequest(deactivateRequest), () -> HttpBindingUtil.toHttpBodyState(deactivateState), executionState);

		// [before driver write deactivate]

		final Consumer<Map<String, Object>> beforeDriverWriteDeactivateConsumer = requestMap -> {
			try {
				LocalUniRegistrar.this.executeExtensions(Extension.BeforeDriverWriteDeactivateExtension.class, e -> e.beforeDriverWriteDeactivate(method, requestMap, LocalUniRegistrar.this), requestMap);
			} catch (RegistrationException ex) {
				throw new RuntimeException(ex.getMessage(), ex);
			}
		};

		// [before driver read deactivate]

		final Consumer<Map<String, Object>> beforeDriverReadDeactivateConsumer = stateMap -> {
			try {
				LocalUniRegistrar.this.executeExtensions(Extension.BeforeDriverReadDeactivateExtension.class, e -> e.beforeDriverReadDeactivate(method, stateMap, LocalUniRegistrar.this), stateMap);
			} catch (RegistrationException ex) {
				throw new RuntimeException(ex.getMessage(), ex);
			}
		};

		// [deactivate]

		if (! extensionStatus.skipDriver()) {

			Driver driver = this.getDrivers().get(method);
			if (driver == null) throw new RegistrationException(RegistrationException.ERROR_BADREQUEST, "Unsupported method: " + method);
			if (log.isInfoEnabled()) log.info("Executing deactivate with request " + deactivateRequest + " with driver " + driver.getClass().getSimpleName());

			if (driver instanceof HttpDriver httpDriver) {
				httpDriver.setBeforeWriteDeactivateConsumer(beforeDriverWriteDeactivateConsumer);
				httpDriver.setBeforeReadDeactivateConsumer(beforeDriverReadDeactivateConsumer);
			}

			DeactivateState driverDeactivateState = driver.deactivate(deactivateRequest);
			if (driverDeactivateState != null) {
				deactivateState.setJobId(driverDeactivateState.getJobId());
				deactivateState.setDidState(driverDeactivateState.getDidState());
				if (driverDeactivateState.getDidRegistrationMetadata() != null) deactivateState.getDidRegistrationMetadata().putAll(driverDeactivateState.getDidRegistrationMetadata());
				if (driverDeactivateState.getDidDocumentMetadata() != null) deactivateState.getDidDocumentMetadata().putAll(driverDeactivateState.getDidDocumentMetadata());
			}

			if (log.isInfoEnabled()) log.info("Executed deactivate with state " + deactivateState + " with driver " + driver.getClass().getSimpleName());
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

	public ExecuteState execute(String method, ExecuteRequest executeRequest, Map<String, Object> initialExecutionState) throws RegistrationException {

		if (method == null) throw new NullPointerException();
		if (executeRequest == null) throw new NullPointerException();

		if (this.getDrivers() == null) throw new RegistrationException("No drivers configured.");

		// start time

		long start = System.currentTimeMillis();

		// prepare execution state

		Map<String, Object> executionState = new HashMap<>();
		if (initialExecutionState != null) executionState.putAll(initialExecutionState);

		// prepare execute state

		ExecuteState executeState = new ExecuteState();
		ExtensionStatus extensionStatus = new ExtensionStatus();

		// [before execute]

		this.executeExtensions(Extension.BeforeExecuteExtension.class, extensionStatus, e -> e.beforeExecute(method, executeRequest, executeState, executionState, this), () -> HttpBindingUtil.toHttpBodyRequest(executeRequest), () -> HttpBindingUtil.toHttpBodyState(executeState), executionState);

		// [before driver write execute]

		final Consumer<Map<String, Object>> beforeDriverWriteExecuteConsumer = requestMap -> {
			try {
				LocalUniRegistrar.this.executeExtensions(Extension.BeforeDriverWriteExecuteExtension.class, e -> e.beforeDriverWriteExecute(method, requestMap, LocalUniRegistrar.this), requestMap);
			} catch (RegistrationException ex) {
				throw new RuntimeException(ex.getMessage(), ex);
			}
		};

		// [before driver read execute]

		final Consumer<Map<String, Object>> beforeDriverReadExecuteConsumer = stateMap -> {
			try {
				LocalUniRegistrar.this.executeExtensions(Extension.BeforeDriverReadExecuteExtension.class, e -> e.beforeDriverReadExecute(method, stateMap, LocalUniRegistrar.this), stateMap);
			} catch (RegistrationException ex) {
				throw new RuntimeException(ex.getMessage(), ex);
			}
		};

		// [execute]

		if (! extensionStatus.skipDriver()) {

			Driver driver = this.getDrivers().get(method);
			if (driver == null) throw new RegistrationException(RegistrationException.ERROR_BADREQUEST, "Unsupported method: " + method);
			if (log.isInfoEnabled()) log.info("Executing execute with request " + executeRequest + " with driver " + driver.getClass().getSimpleName());

			if (driver instanceof HttpDriver httpDriver) {
				httpDriver.setBeforeWriteExecuteConsumer(beforeDriverWriteExecuteConsumer);
				httpDriver.setBeforeReadExecuteConsumer(beforeDriverReadExecuteConsumer);
			}

			ExecuteState driverExecuteState = driver.execute(executeRequest);
			if (driverExecuteState != null) {
				executeState.setJobId(driverExecuteState.getJobId());
				executeState.setDidState(driverExecuteState.getDidState());
				if (driverExecuteState.getDidRegistrationMetadata() != null) executeState.getDidRegistrationMetadata().putAll(driverExecuteState.getDidRegistrationMetadata());
				if (driverExecuteState.getDidDocumentMetadata() != null) executeState.getDidDocumentMetadata().putAll(driverExecuteState.getDidDocumentMetadata());
			}

			if (log.isInfoEnabled()) log.info("Executed execute with state " + executeState + " with driver " + driver.getClass().getSimpleName());
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

	public CreateResourceState createResource(String method, CreateResourceRequest createResourceRequest, Map<String, Object> initialExecutionState) throws RegistrationException {

		if (method == null) throw new NullPointerException();
		if (createResourceRequest == null) throw new NullPointerException();

		if (this.getDrivers() == null) throw new RegistrationException("No drivers configured.");

		// start time

		long start = System.currentTimeMillis();

		// prepare execution state

		Map<String, Object> executionState = new HashMap<>();
		if (initialExecutionState != null) executionState.putAll(initialExecutionState);

		// prepare createResource state

		CreateResourceState createResourceState = new CreateResourceState();
		ExtensionStatus extensionStatus = new ExtensionStatus();

		// [before createResource]

		this.executeExtensions(Extension.BeforeCreateResourceExtension.class, extensionStatus, e -> e.beforeCreateResource(method, createResourceRequest, createResourceState, executionState, this), () -> HttpBindingUtil.toHttpBodyRequest(createResourceRequest), () -> HttpBindingUtil.toHttpBodyResourceState(createResourceState), executionState);

		// [before driver write createResource]

		final Consumer<Map<String, Object>> beforeDriverWriteCreateResourceConsumer = requestMap -> {
			try {
				LocalUniRegistrar.this.executeExtensions(Extension.BeforeDriverWriteCreateResourceExtension.class, e -> e.beforeDriverWriteCreateResource(method, requestMap, LocalUniRegistrar.this), requestMap);
			} catch (RegistrationException ex) {
				throw new RuntimeException(ex.getMessage(), ex);
			}
		};

		// [before driver read createResource]

		final Consumer<Map<String, Object>> beforeDriverReadCreateResourceConsumer = stateMap -> {
			try {
				LocalUniRegistrar.this.executeExtensions(Extension.BeforeDriverReadCreateResourceExtension.class, e -> e.beforeDriverReadCreateResource(method, stateMap, LocalUniRegistrar.this), stateMap);
			} catch (RegistrationException ex) {
				throw new RuntimeException(ex.getMessage(), ex);
			}
		};

		// [createResource]

		if (! extensionStatus.skipDriver()) {

			Driver driver = this.getDrivers().get(method);
			if (driver == null) throw new RegistrationException(RegistrationException.ERROR_BADREQUEST, "Unsupported method: " + method);
			if (log.isInfoEnabled()) log.info("Executing createResource with request " + createResourceRequest + " with driver " + driver.getClass().getSimpleName());

			if (driver instanceof HttpDriver httpDriver) {
				httpDriver.setBeforeWriteCreateResourceConsumer(beforeDriverWriteCreateResourceConsumer);
				httpDriver.setBeforeReadCreateResourceConsumer(beforeDriverReadCreateResourceConsumer);
			}

			CreateResourceState driverCreateResourceState = driver.createResource(createResourceRequest);
			if (driverCreateResourceState != null) {
				createResourceState.setJobId(driverCreateResourceState.getJobId());
				createResourceState.setDidUrlState(driverCreateResourceState.getDidUrlState());
				if (driverCreateResourceState.getDidRegistrationMetadata() != null) createResourceState.getDidRegistrationMetadata().putAll(driverCreateResourceState.getDidRegistrationMetadata());
				if (driverCreateResourceState.getContentMetadata() != null) createResourceState.getContentMetadata().putAll(driverCreateResourceState.getContentMetadata());
			}

			if (log.isInfoEnabled()) log.info("Executed createResource resource with state " + createResourceState + " with driver " + driver.getClass().getSimpleName());
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

	public UpdateResourceState updateResource(String method, UpdateResourceRequest updateResourceRequest, Map<String, Object> initialExecutionState) throws RegistrationException {

		if (method == null) throw new NullPointerException();
		if (updateResourceRequest == null) throw new NullPointerException();

		if (this.getDrivers() == null) throw new RegistrationException("No drivers configured.");

		// start time

		long start = System.currentTimeMillis();

		// prepare execution state

		Map<String, Object> executionState = new HashMap<>();
		if (initialExecutionState != null) executionState.putAll(initialExecutionState);

		// prepare updateResource state

		UpdateResourceState updateResourceState = new UpdateResourceState();
		ExtensionStatus extensionStatus = new ExtensionStatus();

		// [before updateResource]

		this.executeExtensions(Extension.BeforeUpdateResourceExtension.class, extensionStatus, e -> e.beforeUpdateResource(method, updateResourceRequest, updateResourceState, executionState, this), () -> HttpBindingUtil.toHttpBodyRequest(updateResourceRequest), () -> HttpBindingUtil.toHttpBodyResourceState(updateResourceState), executionState);

		// [before driver write updateResource]

		final Consumer<Map<String, Object>> beforeDriverWriteUpdateResourceConsumer = requestMap -> {
			try {
				LocalUniRegistrar.this.executeExtensions(Extension.BeforeDriverWriteUpdateResourceExtension.class, e -> e.beforeDriverWriteUpdateResource(method, requestMap, LocalUniRegistrar.this), requestMap);
			} catch (RegistrationException ex) {
				throw new RuntimeException(ex.getMessage(), ex);
			}
		};

		// [before driver read updateResource]

		final Consumer<Map<String, Object>> beforeDriverReadUpdateResourceConsumer = stateMap -> {
			try {
				LocalUniRegistrar.this.executeExtensions(Extension.BeforeDriverReadUpdateResourceExtension.class, e -> e.beforeDriverReadUpdateResource(method, stateMap, LocalUniRegistrar.this), stateMap);
			} catch (RegistrationException ex) {
				throw new RuntimeException(ex.getMessage(), ex);
			}
		};

		// [updateResource]

		if (! extensionStatus.skipDriver()) {

			Driver driver = this.getDrivers().get(method);
			if (driver == null) throw new RegistrationException(RegistrationException.ERROR_BADREQUEST, "Unsupported method: " + method);
			if (log.isInfoEnabled()) log.info("Executing updateResource with request " + updateResourceRequest + " with driver " + driver.getClass().getSimpleName());

			if (driver instanceof HttpDriver httpDriver) {
				httpDriver.setBeforeWriteUpdateResourceConsumer(beforeDriverWriteUpdateResourceConsumer);
				httpDriver.setBeforeReadUpdateResourceConsumer(beforeDriverReadUpdateResourceConsumer);
			}

			UpdateResourceState driverUpdateResourceState = driver.updateResource(updateResourceRequest);
			if (driverUpdateResourceState != null) {
				updateResourceState.setJobId(driverUpdateResourceState.getJobId());
				updateResourceState.setDidUrlState(driverUpdateResourceState.getDidUrlState());
				if (driverUpdateResourceState.getDidRegistrationMetadata() != null) updateResourceState.getDidRegistrationMetadata().putAll(driverUpdateResourceState.getDidRegistrationMetadata());
				if (driverUpdateResourceState.getContentMetadata() != null) updateResourceState.getContentMetadata().putAll(driverUpdateResourceState.getContentMetadata());
			}

			if (log.isInfoEnabled()) log.info("Executed updateResource with state " + updateResourceState + " with driver " + driver.getClass().getSimpleName());
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

	public DeactivateResourceState deactivateResource(String method, DeactivateResourceRequest deactivateResourceRequest, Map<String, Object> initialExecutionState) throws RegistrationException {

		if (method == null) throw new NullPointerException();
		if (deactivateResourceRequest == null) throw new NullPointerException();

		if (this.getDrivers() == null) throw new RegistrationException("No drivers configured.");

		// start time

		long start = System.currentTimeMillis();

		// prepare execution state

		Map<String, Object> executionState = new HashMap<>();
		if (initialExecutionState != null) executionState.putAll(initialExecutionState);

		// prepare deactivateResource state

		DeactivateResourceState deactivateResourceState = new DeactivateResourceState();
		ExtensionStatus extensionStatus = new ExtensionStatus();

		// [before deactivateResource]

		this.executeExtensions(Extension.BeforeDeactivateResourceExtension.class, extensionStatus, e -> e.beforeDeactivateResource(method, deactivateResourceRequest, deactivateResourceState, executionState, this), () -> HttpBindingUtil.toHttpBodyRequest(deactivateResourceRequest), () -> HttpBindingUtil.toHttpBodyResourceState(deactivateResourceState), executionState);

		// [before driver write deactivateResource]

		final Consumer<Map<String, Object>> beforeDriverWriteDeactivateResourceConsumer = requestMap -> {
			try {
				LocalUniRegistrar.this.executeExtensions(Extension.BeforeDriverWriteDeactivateExtension.class, e -> e.beforeDriverWriteDeactivate(method, requestMap, LocalUniRegistrar.this), requestMap);
			} catch (RegistrationException ex) {
				throw new RuntimeException(ex.getMessage(), ex);
			}
		};

		// [before driver read deactivateResource]

		final Consumer<Map<String, Object>> beforeDriverReadDeactivateResourceConsumer = stateMap -> {
			try {
				LocalUniRegistrar.this.executeExtensions(Extension.BeforeDriverReadDeactivateExtension.class, e -> e.beforeDriverReadDeactivate(method, stateMap, LocalUniRegistrar.this), stateMap);
			} catch (RegistrationException ex) {
				throw new RuntimeException(ex.getMessage(), ex);
			}
		};

		// [deactivateResource]

		if (! extensionStatus.skipDriver()) {

			Driver driver = this.getDrivers().get(method);
			if (driver == null) throw new RegistrationException(RegistrationException.ERROR_BADREQUEST, "Unsupported method: " + method);
			if (log.isInfoEnabled()) log.info("Executing deactivateResource with request " + deactivateResourceRequest + " with driver " + driver.getClass().getSimpleName());

			if (driver instanceof HttpDriver httpDriver) {
				httpDriver.setBeforeWriteDeactivateConsumer(beforeDriverWriteDeactivateResourceConsumer);
				httpDriver.setBeforeReadDeactivateConsumer(beforeDriverReadDeactivateResourceConsumer);
			}

			DeactivateResourceState driverDeactivateState = driver.deactivateResource(deactivateResourceRequest);
			if (driverDeactivateState != null) {
				deactivateResourceState.setJobId(driverDeactivateState.getJobId());
				deactivateResourceState.setDidUrlState(driverDeactivateState.getDidUrlState());
				if (driverDeactivateState.getDidRegistrationMetadata() != null) deactivateResourceState.getDidRegistrationMetadata().putAll(driverDeactivateState.getDidRegistrationMetadata());
				if (driverDeactivateState.getContentMetadata() != null) deactivateResourceState.getContentMetadata().putAll(driverDeactivateState.getContentMetadata());
			}

			if (log.isInfoEnabled()) log.info("Executed deactivateResource with state " + deactivateResourceState + " with driver " + driver.getClass().getSimpleName());
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

		if (this.getDrivers() == null) throw new RegistrationException("No drivers configured.");

		Map<String, Map<String, Object>> properties = new LinkedHashMap<>();

		for (Entry<String, Driver> driver : this.getDrivers().entrySet()) {

			if (log.isDebugEnabled()) log.debug("Loading properties for driver " + driver.getKey() + " (" + driver.getValue().getClass().getSimpleName() + ")");

			Map<String, Object> driverProperties = driver.getValue().properties();
			if (driverProperties == null) driverProperties = Collections.emptyMap();

			properties.put(driver.getKey(), driverProperties);
		}

		// done

		if (log.isDebugEnabled()) log.debug("Loaded properties: " + properties);
		return properties;
	}

	@Override
	public Set<String> methods() throws RegistrationException {

		if (this.getDrivers() == null) throw new RegistrationException("No drivers configured.");

		Set<String> methods = this.getDrivers().keySet();

		// done

		if (log.isDebugEnabled()) log.debug("Loaded methods: " + methods);
		return methods;
	}

	/*
	 * Getters and setters
	 */

	public Map<String, Driver> getDrivers() {
		return this.drivers;
	}

	@SuppressWarnings("unchecked")
	public <T extends Driver> T getDriver(Class<T> driverClass) {
		for (Driver driver : this.getDrivers().values()) {
			if (driverClass.isAssignableFrom(driver.getClass())) return (T) driver;
		}
		return null;
	}

	public void setDrivers(Map<String, Driver> drivers) {
		this.drivers = drivers;
	}

	public List<Extension> getExtensions() {
		return this.extensions;
	}

	public void setExtensions(List<Extension> extensions) {
		this.extensions = extensions;
	}
}
