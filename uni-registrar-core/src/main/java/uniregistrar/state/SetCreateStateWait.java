package uniregistrar.state;

public class SetCreateStateWait {

	private SetCreateStateWait() {

	}

	public static boolean isStateWait(CreateState createState) {

		return "wait".equals(SetCreateState.getState(createState));
	}

	public static String getStateWait(CreateState createState) {

		if (! isStateWait(createState)) return null;
		return (String) createState.getDidState().get("wait");
	}

	public static String getStateWaittime(CreateState createState) {

		if (! isStateWait(createState)) return null;
		return (String) createState.getDidState().get("waittime");
	}

	public static void setStateWait(CreateState createState, String wait, String waittime) {

		SetCreateState.setState(createState, "wait");
		createState.getDidState().put("wait", wait);
		createState.getDidState().put("waittime", waittime);
	}
}
