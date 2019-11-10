package uniregistrar.state;

import java.util.Map;

public class SetRegisterStateFinished {

	private SetRegisterStateFinished() {

	}

	public static boolean isStateFinished(RegisterState registerState) {

		return "finished".equals(SetRegisterState.getState(registerState));
	}

	public static String getStateFinishedIdentifier(RegisterState registerState) {

		if (! isStateFinished(registerState)) return null;
		return (String) registerState.getDidState().get("identifier");
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> getStateFinishedSecret(RegisterState registerState) {

		if (! isStateFinished(registerState)) return null;
		return (Map<String, Object>) registerState.getDidState().get("secret");
	}

	public static void setStateFinished(RegisterState registerState, String identifier, Map<String, Object> secret) {

		SetRegisterState.setState(registerState, "finished");
		registerState.getDidState().put("identifier", identifier);
		registerState.getDidState().put("secret", secret);
	}
}
