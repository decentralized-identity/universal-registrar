package uniregistrar.state;

public class SetRegisterState {

	private SetRegisterState() {

	}

	public static String getState(RegisterState registerState) {

		return (String) registerState.getDidState().get("state");
	}

	public static void setState(RegisterState registerState, String state) {

		registerState.getDidState().put("state", state);
	}
}
