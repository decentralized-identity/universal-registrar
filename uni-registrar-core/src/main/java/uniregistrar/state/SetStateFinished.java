package uniregistrar.state;

import java.util.Map;

public class SetStateFinished {

	private SetStateFinished() {

	}

	public static boolean isStateFinished(State state) {

		return "finished".equals(SetState.getState(state));
	}

	public static String getStateFinishedDid(State state) {

		if (! isStateFinished(state)) return null;
		return (String) state.getDidState().get("did");
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> getStateFinishedSecret(State state) {

		if (! isStateFinished(state)) return null;
		return (Map<String, Object>) state.getDidState().get("secret");
	}

	public static void setStateFinished(State state, String did, Map<String, Object> secret) {
		// Remove interim states if they exist
		state.getDidState().remove("wait");
		state.getDidState().remove("action");

		SetState.setState(state, "finished");
		state.getDidState().put("did", did);
		state.getDidState().put("secret", secret);
	}
}
