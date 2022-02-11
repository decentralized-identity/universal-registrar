package uniregistrar.state;

public class SetStateFailed {

	private SetStateFailed() {

	}

	public static boolean isStateFailed(State state) {

		return "failed".equals(SetState.getState(state));
	}

	public static String getStateFailedError(State state) {

		if (! isStateFailed(state)) return null;
		return (String) state.getDidState().get("error");
	}

	public static String getStateFailedReason(State state) {

		if (! isStateFailed(state)) return null;
		return (String) state.getDidState().get("reason");
	}

	public static void setStateFailed(State state, String error, String reason) {
		// Remove interim states if they exist
		state.getDidState().remove("wait");
		state.getDidState().remove("action");

		SetState.setState(state, "failed");
		state.getDidState().put("error", error);
		state.getDidState().put("reason", reason);
	}
}
