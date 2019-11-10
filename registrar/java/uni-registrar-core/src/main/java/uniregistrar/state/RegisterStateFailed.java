package uniregistrar.state;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class RegisterStateFailed {

	private RegisterStateFailed() {

	}

	@JsonIgnore
	public static boolean isStateFailed(RegisterState registerState) {

		return "failed".equals(RegisterState.getState(registerState));
	}

	@JsonIgnore
	public static String getStateFailedReason(RegisterState registerState) {

		if (! isStateFailed(registerState)) return null;
		return (String) registerState.getDidState().get("reason");
	}

	@JsonIgnore
	public static void setStateFailed(RegisterState registerState, String reason) {

		RegisterState.setState(registerState, "failed");
		registerState.getDidState().put("reason", reason);
	}
}
