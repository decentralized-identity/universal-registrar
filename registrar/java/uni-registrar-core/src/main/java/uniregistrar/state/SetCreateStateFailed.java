package uniregistrar.state;

public class SetCreateStateFailed {

	private SetCreateStateFailed() {

	}

	public static boolean isStateFailed(CreateState createState) {

		return "failed".equals(SetCreateState.getState(createState));
	}

	public static String getStateFailedReason(CreateState createState) {

		if (! isStateFailed(createState)) return null;
		return (String) createState.getDidState().get("reason");
	}

	public static void setStateFailed(CreateState createState, String reason) {

		SetCreateState.setState(createState, "failed");
		createState.getDidState().put("reason", reason);
	}
}
