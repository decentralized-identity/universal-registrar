package uniregistrar.state;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class RegisterStateAction {

	private RegisterStateAction() {

	}

	@JsonIgnore
	public static boolean isStateAction(RegisterState registerState) {

		return "action".equals(RegisterState.getState(registerState));
	}

	@JsonIgnore
	public static String getStateAction(RegisterState registerState) {

		if (! isStateAction(registerState)) return null;
		return (String) registerState.getDidState().get("action");
	}

	@JsonIgnore
	public static void setStateAction(RegisterState registerState, String action) {

		RegisterState.setState(registerState, "action");
		registerState.getDidState().put("action", action);
	}
}
