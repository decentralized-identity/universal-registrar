package uniregistrar.state;

public class SetCreateStateAction {

	private SetCreateStateAction() {

	}

	public static boolean isStateAction(CreateState createState) {

		return "action".equals(SetCreateState.getState(createState));
	}

	public static String getStateAction(CreateState createState) {

		if (! isStateAction(createState)) return null;
		return (String) createState.getDidState().get("action");
	}

	public static void setStateAction(CreateState createState, String action) {

		SetCreateState.setState(createState, "action");
		createState.getDidState().put("action", action);
	}
}
