package uniregistrar.state;

public class SetState {

	private SetState() {

	}

	public static String getState(State state) {

		return (String) state.getDidState().get("state");
	}

	public static void setState(State state, String stateString) {

		state.getDidState().put("state", stateString);
	}
}
