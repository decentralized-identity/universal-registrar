package uniregistrar.state;

public class SetStateWait {

	private SetStateWait() {

	}

	public static boolean isStateWait(State state) {

		return "wait".equals(SetState.getState(state));
	}

	public static String getStateWait(State state) {

		if (! isStateWait(state)) return null;
		return (String) state.getDidState().get("wait");
	}

	public static String getStateWaittime(State state) {

		if (! isStateWait(state)) return null;
		return (String) state.getDidState().get("waittime");
	}

	public static void setStateWait(State state, String wait, String waittime) {

		SetState.setState(state, "wait");
		state.getDidState().put("wait", wait);
		state.getDidState().put("waittime", waittime);
	}
}
