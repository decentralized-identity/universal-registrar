package uniregistrar.state;

public class SetCreateState {

	private SetCreateState() {

	}

	public static String getState(CreateState createState) {

		return (String) createState.getDidState().get("state");
	}

	public static void setState(CreateState createState, String state) {

		createState.getDidState().put("state", state);
	}
}
