package uniregistrar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniregistrar.state.SetStateFailed;
import uniregistrar.state.State;

import java.util.Map;

public class RegistrationException extends Exception {

	public static final String ERROR_BADREQUEST = "badRequest";
	public static final String ERROR_NOTFOUND = "notFound";
	public static final String ERROR_INTERNALERROR = "internalError";

	private static final Logger log = LoggerFactory.getLogger(RegistrationException.class);

	private final String error;
	private final Map<String, Object> didRegistrationMetadata;

	private final State state;

	public RegistrationException(String error, String message, Map<String, Object> didRegistrationMetadata, Throwable ex) {
		super(message, ex);
		this.error = error;
		this.didRegistrationMetadata = didRegistrationMetadata;
		this.state = null;
	}

	public RegistrationException(String error, String message, Map<String, Object> didRegistrationMetadata) {
		super(message);
		this.error = error;
		this.didRegistrationMetadata = didRegistrationMetadata;
		this.state = null;
	}

	public RegistrationException(String error, String message, Throwable ex) {
		this(error, message, null, ex);
	}

	public RegistrationException(String error, String message) {
		this(error, message, null, null);
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

	public RegistrationException(State state) {
		super(SetStateFailed.getStateFailedReason(state));
		if (! SetStateFailed.isStateFailed(state)) throw new IllegalArgumentException("No failed state: " + state);
		this.error = SetStateFailed.getStateFailedError(state);
		this.didRegistrationMetadata = state.getDidRegistrationMetadata();
		this.state = state;
	}

	/*
	 * Error methods
	 */

	public State toFailedState() {
		if (this.getState() != null) {
			return this.getState();
		} else {
			State state = State.build();
			SetStateFailed.setStateFailed(state, this.getError(), this.getMessage());
			if (this.getDidRegistrationMetadata() != null) state.setDidRegistrationMetadata(this.getDidRegistrationMetadata());
			state.setJobId(null);
			if (log.isDebugEnabled()) log.debug("Created failed state: " + state);
			return state;
		}
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

	public State getState() {
		return state;
	}
}
