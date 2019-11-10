package uniregistrar.state;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class RegisterStateFinished {

	private RegisterStateFinished() {

	}

	@JsonIgnore
	public static boolean isStateFinished(RegisterState registerState) {

		return "finished".equals(RegisterState.getState(registerState));
	}

	@JsonIgnore
	public static String getStateFinishedIdentifier(RegisterState registerState) {

		if (! isStateFinished(registerState)) return null;
		return (String) registerState.getDidState().get("identifier");
	}

	@JsonIgnore
	@SuppressWarnings("unchecked")
	public static Map<String, Object> getStateFinishedSecret(RegisterState registerState) {

		if (! isStateFinished(registerState)) return null;
		return (Map<String, Object>) registerState.getDidState().get("secret");
	}

	@JsonIgnore
	public static void setStateFinished(RegisterState registerState, String identifier, Map<String, Object> secret) {

		RegisterState.setState(registerState, "finished");
		registerState.getDidState().put("identifier", identifier);
		registerState.getDidState().put("secret", secret);
	}
}
