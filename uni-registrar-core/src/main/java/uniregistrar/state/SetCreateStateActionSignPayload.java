package uniregistrar.state;

public final class SetCreateStateActionSignPayload {

	private SetCreateStateActionSignPayload() {

	}

	public static boolean isStateActionSignPayload(CreateState createState) {

		return "signPayload".equals(SetCreateStateAction.getStateAction(createState));
	}

	public static String getStateActionSignPayloadPayload(CreateState createState) {

		if (!isStateActionSignPayload(createState)) return null;
		return (String) createState.getDidState().get("payload");
	}

	public static String getStateActionSignPayloadKid(CreateState createState) {

		if (!isStateActionSignPayload(createState)) return null;
		return (String) createState.getDidState().get("kid");
	}

	public static String getStateActionSignPayloadAlg(CreateState createState) {

		if (!isStateActionSignPayload(createState)) return null;
		return (String) createState.getDidState().get("alg");
	}

	public static void setStateActionSignPayload(CreateState createState, String payload, String kid, String alg) {

		SetCreateStateAction.setStateAction(createState, "signPayload");
		createState.getDidState().put("payload", payload);
		createState.getDidState().put("kid", kid);
		createState.getDidState().put("alg", alg);
	}
}
