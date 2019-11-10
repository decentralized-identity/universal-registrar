package uniregistrar.state;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class RegisterStateStep {

	private RegisterStateStep() {

	}

	@JsonIgnore
	public static boolean isStateFinished(RegisterState registerState) {

		return "finished".equals((String) registerState.getDidState().get("state"));
	}

	@JsonIgnore
	public static String getFinishedIdentifier(RegisterState registerState) {

		if (! isStateFinished(registerState)) return null;
		return (String) registerState.getDidState().get("identifier");
	}

	@JsonIgnore
	public static String getFinishedSecret(RegisterState registerState) {

		if (! isStateFinished(registerState)) return null;
		return (String) registerState.getDidState().get("secret");
	}

	@JsonIgnore
	public static void setStateFinished(RegisterState registerState, String identifier, Map<String, Object> secret) {

		registerState.getDidState().put("state", "finished");
		registerState.getDidState().put("identifier", identifier);
		registerState.getDidState().put("secret", secret);
	}

}
