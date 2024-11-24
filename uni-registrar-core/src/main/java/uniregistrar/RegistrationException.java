package uniregistrar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniregistrar.openapi.model.*;

public class RegistrationException extends Exception {

	public static final String ERROR_BADREQUEST = "badRequest";
	public static final String ERROR_NOTFOUND = "notFound";
	public static final String ERROR_INTERNALERROR = "internalError";

	private static final Logger log = LoggerFactory.getLogger(RegistrationException.class);

	private final String error;
	private final RegistrarState registrarState;
	private final RegistrarResourceState registrarResourceState;

	private RegistrationException(String error, String message, RegistrarState registrarState, RegistrarResourceState registrarResourceState, Throwable ex) {
		super(message, ex);
		this.error = error;
		this.registrarState = registrarState != null ? registrarState : makeRegistrarState(this);
		this.registrarResourceState = registrarResourceState != null ? registrarResourceState : makeRegistrarResourceState(this);
	}

	private RegistrationException(String error, String message, RegistrarState registrarState, RegistrarResourceState registrarResourceState) {
		super(message);
		this.error = error;
		this.registrarState = registrarState != null ? registrarState : makeRegistrarState(this);
		this.registrarResourceState = registrarResourceState != null ? registrarResourceState : makeRegistrarResourceState(this);
	}

	public RegistrationException(String error, String message, Throwable ex) {
		this(error, message, (RegistrarState) null, (RegistrarResourceState) null, ex);
	}

	public RegistrationException(String error, String message) {
		this(error, message, (RegistrarState) null, (RegistrarResourceState) null);
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
			return new RegistrationException(didStateFailed.getError(), didStateFailed.getReason(), registrarState, null);
		} else {
			throw new IllegalArgumentException("No failed state: " + (registrarState == null ? null : registrarState.getDidState()));
		}
	}

	public static RegistrationException fromRegistrarResourceState(RegistrarResourceState registrarResourceState) {
		if (registrarResourceState != null && registrarResourceState.getDidUrlState() instanceof DidUrlStateFailed didUrlStateFailed) {
			return new RegistrationException(didUrlStateFailed.getError(), didUrlStateFailed.getReason(), null, registrarResourceState);
		} else {
			throw new IllegalArgumentException("No failed state: " + (registrarResourceState == null ? null : registrarResourceState.getDidUrlState()));
		}
	}

	private static RegistrarState makeRegistrarState(RegistrationException registrationException) {
		RegistrarState registrarState = new RegistrarState();
		DidStateFailed didStateFailed = new DidStateFailed();
		didStateFailed.setError(registrationException.getError());
		didStateFailed.setReason(registrationException.getMessage());
		registrarState.setDidState(didStateFailed);
		if (log.isDebugEnabled()) log.debug("Created failed registrar state: " + registrarState);
		return registrarState;
	}

	private static RegistrarResourceState makeRegistrarResourceState(RegistrationException registrationException) {
		RegistrarResourceState registrarResourceState = new RegistrarResourceState();
		DidUrlStateFailed didUrlStateFailed = new DidUrlStateFailed();
		didUrlStateFailed.setError(registrationException.getError());
		didUrlStateFailed.setReason(registrationException.getMessage());
		registrarResourceState.setDidUrlState(didUrlStateFailed);
		if (log.isDebugEnabled()) log.debug("Created failed registrar resource state: " + registrarResourceState);
		return registrarResourceState;
	}

	/*
	 * Getters and setters
	 */

	public String getError() {
		return error;
	}

	public RegistrarState getRegistrarState() {
		return registrarState;
	}

	public RegistrarResourceState getRegistrarResourceState() {
		return registrarResourceState;
	}
}
