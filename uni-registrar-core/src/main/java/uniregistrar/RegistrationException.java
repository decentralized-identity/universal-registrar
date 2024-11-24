package uniregistrar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniregistrar.openapi.model.DidStateFailed;
import uniregistrar.openapi.model.DidUrlStateFailed;
import uniregistrar.openapi.model.RegistrarResourceState;
import uniregistrar.openapi.model.RegistrarState;

import java.util.Map;

public class RegistrationException extends Exception {

	public static final String ERROR_BADREQUEST = "badRequest";
	public static final String ERROR_NOTFOUND = "notFound";
	public static final String ERROR_INTERNALERROR = "internalError";

	private static final Logger log = LoggerFactory.getLogger(RegistrationException.class);

	private final String error;
	private final Map<String, Object> didRegistrationMetadata;

	private RegistrarState registrarState;
	private RegistrarResourceState registrarResourceState;

	private RegistrationException(String error, String message, Map<String, Object> didRegistrationMetadata, Throwable ex) {
		super(message, ex);
		this.error = error;
		this.didRegistrationMetadata = didRegistrationMetadata;
	}

	private RegistrationException(String error, String message, Map<String, Object> didRegistrationMetadata) {
		super(message);
		this.error = error;
		this.didRegistrationMetadata = didRegistrationMetadata;
	}

	public RegistrationException(String error, String message, Throwable ex) {
		this(error, message, (Map<String, Object>) null, ex);
	}

	public RegistrationException(String error, String message) {
		this(error, message, (Map<String, Object>) null);
	}

	public RegistrationException(String message, Throwable ex) {
		this(ERROR_INTERNALERROR, message, ex);
	}

	public RegistrationException(String message) {
		this(ERROR_INTERNALERROR, message);
	}

	public RegistrationException(Throwable ex) {
		this(ex.getMessage(), ex);
	}

	public static RegistrationException fromRegistrarState(RegistrarState registrarState) {
		if (registrarState != null && registrarState.getDidState() instanceof DidStateFailed didStateFailed) {
			RegistrationException registrationException = new RegistrationException(didStateFailed.getError(), didStateFailed.getReason(), registrarState.getDidRegistrationMetadata());
			registrationException.registrarState = registrarState;
			return registrationException;
		} else {
			throw new IllegalArgumentException("No failed state: " + (registrarState == null ? null : registrarState.getDidState()));
		}
	}

	public static RegistrationException fromRegistrarResourceState(RegistrarResourceState registrarResourceState) {
		if (registrarResourceState != null && registrarResourceState.getDidUrlState() instanceof DidUrlStateFailed didUrlStateFailed) {
			RegistrationException registrationException = new RegistrationException(didUrlStateFailed.getError(), didUrlStateFailed.getReason(), registrarResourceState.getDidRegistrationMetadata());
			registrationException.registrarResourceState = registrarResourceState;
			return registrationException;
		} else {
			throw new IllegalArgumentException("No failed state: " + (registrarResourceState == null ? null : registrarResourceState.getDidUrlState()));
		}
	}

	/*
	 * Error methods
	 */

	public RegistrarState toErrorRegistrarState() {
		if (this.registrarState != null) return this.registrarState;
		RegistrarState registrarState = new RegistrarState();
		DidStateFailed didStateFailed = new DidStateFailed();
		if (this.getError() != null) didStateFailed.setError(this.getError());
		if (this.getMessage() != null) didStateFailed.setReason(this.getMessage());
		registrarState.setDidState(didStateFailed);
		if (this.getDidRegistrationMetadata() != null) registrarState.getDidRegistrationMetadata().putAll(this.getDidRegistrationMetadata());
		if (log.isDebugEnabled()) log.debug("Created failed registrar state: " + registrarState);
		return registrarState;
	}

	public RegistrarResourceState toErrorRegistrarResourceState() {
		if (this.registrarResourceState != null) return this.registrarResourceState;
		RegistrarResourceState registrarResourceState = new RegistrarResourceState();
		DidUrlStateFailed didUrlStateFailed = new DidUrlStateFailed();
		if (this.getError() != null) didUrlStateFailed.setError(this.getError());
		if (this.getMessage() != null) didUrlStateFailed.setReason(this.getMessage());
		registrarResourceState.setDidUrlState(didUrlStateFailed);
		if (this.getDidRegistrationMetadata() != null) registrarResourceState.getDidRegistrationMetadata().putAll(this.getDidRegistrationMetadata());
		if (log.isDebugEnabled()) log.debug("Created failed registrar resource state: " + registrarResourceState);
		return registrarResourceState;
	}

	/*
	 * Getters and setters
	 */

	public String getError() {
		return error;
	}

	public Map<String, Object> getDidRegistrationMetadata() {
		return didRegistrationMetadata;
	}
}
