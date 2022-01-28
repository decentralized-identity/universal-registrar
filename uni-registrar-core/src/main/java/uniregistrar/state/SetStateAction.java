package uniregistrar.state;

public class SetStateAction {

	private SetStateAction() {

	}

	public static boolean isStateAction(State state) {

		return "action".equals(SetState.getState(state));
	}

	public static String getStateAction(State state) {

		if (! isStateAction(state)) return null;
		return (String) state.getDidState().get("action");
	}

	public static void setStateAction(State state, String action) {

		SetState.setState(state, "action");
		state.getDidState().put("action", action);
	}
}
