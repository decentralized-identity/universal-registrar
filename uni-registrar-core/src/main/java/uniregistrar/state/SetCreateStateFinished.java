package uniregistrar.state;

import java.util.Map;

public class SetCreateStateFinished {

	private SetCreateStateFinished() {

	}

	public static boolean isStateFinished(CreateState createState) {

		return "finished".equals(SetCreateState.getState(createState));
	}

	public static String getStateFinishedDid(CreateState createState) {

		if (! isStateFinished(createState)) return null;
		return (String) createState.getDidState().get("did");
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> getStateFinishedSecret(CreateState createState) {

		if (! isStateFinished(createState)) return null;
		return (Map<String, Object>) createState.getDidState().get("secret");
	}

	public static void setStateFinished(CreateState createState, String did, Map<String, Object> secret) {

		SetCreateState.setState(createState, "finished");
		createState.getDidState().put("did", did);
		createState.getDidState().put("secret", secret);
	}
}
