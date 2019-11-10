package uniregistrar.state;

public class SetRegisterStateFailed {

	private SetRegisterStateFailed() {

	}

	public static boolean isStateFailed(RegisterState registerState) {

		return "failed".equals(SetRegisterState.getState(registerState));
	}

	public static String getStateFailedReason(RegisterState registerState) {

		if (! isStateFailed(registerState)) return null;
		return (String) registerState.getDidState().get("reason");
	}

	public static void setStateFailed(RegisterState registerState, String reason) {

		SetRegisterState.setState(registerState, "failed");
		registerState.getDidState().put("reason", reason);
	}
}
