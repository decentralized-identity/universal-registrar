package uniregistrar.state;

public class SetRegisterStateWait {

	private SetRegisterStateWait() {

	}

	public static boolean isStateWait(RegisterState registerState) {

		return "wait".equals(SetRegisterState.getState(registerState));
	}

	public static String getStateWait(RegisterState registerState) {

		if (! isStateWait(registerState)) return null;
		return (String) registerState.getDidState().get("wait");
	}

	public static String getStateWaittime(RegisterState registerState) {

		if (! isStateWait(registerState)) return null;
		return (String) registerState.getDidState().get("waittime");
	}

	public static void setStateWait(RegisterState registerState, String wait, String waittime) {

		SetRegisterState.setState(registerState, "wait");
		registerState.getDidState().put("wait", wait);
		registerState.getDidState().put("waittime", waittime);
	}
}
