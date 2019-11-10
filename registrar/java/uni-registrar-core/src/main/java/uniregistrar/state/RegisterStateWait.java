package uniregistrar.state;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class RegisterStateWait {

	private RegisterStateWait() {

	}

	@JsonIgnore
	public static boolean isStateWait(RegisterState registerState) {

		return "wait".equals(RegisterState.getState(registerState));
	}

	@JsonIgnore
	public static String getStateWait(RegisterState registerState) {

		if (! isStateWait(registerState)) return null;
		return (String) registerState.getDidState().get("wait");
	}

	@JsonIgnore
	public static String getStateWaittime(RegisterState registerState) {

		if (! isStateWait(registerState)) return null;
		return (String) registerState.getDidState().get("waittime");
	}

	@JsonIgnore
	public static void setStateWait(RegisterState registerState, String wait, String waittime) {

		RegisterState.setState(registerState, "wait");
		registerState.getDidState().put("wait", wait);
		registerState.getDidState().put("waittime", waittime);
	}
}
