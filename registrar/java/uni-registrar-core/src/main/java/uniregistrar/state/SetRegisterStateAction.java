package uniregistrar.state;

public class SetRegisterStateAction {

	private SetRegisterStateAction() {

	}

	public static boolean isStateAction(RegisterState registerState) {

		return "action".equals(SetRegisterState.getState(registerState));
	}

	public static String getStateAction(RegisterState registerState) {

		if (! isStateAction(registerState)) return null;
		return (String) registerState.getDidState().get("action");
	}

	public static void setStateAction(RegisterState registerState, String action) {

		SetRegisterState.setState(registerState, "action");
		registerState.getDidState().put("action", action);
	}
}
