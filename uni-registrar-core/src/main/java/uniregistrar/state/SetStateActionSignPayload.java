package uniregistrar.state;

public final class SetStateActionSignPayload {

	private SetStateActionSignPayload() {

	}

	public static boolean isStateActionSignPayload(State state) {

		return "signPayload".equals(SetStateAction.getStateAction(state));
	}

	public static String getStateActionSignPayloadPayload(State state) {

		if (!isStateActionSignPayload(state)) return null;
		return (String) state.getDidState().get("payload");
	}

	public static String getStateActionSignPayloadKid(State state) {

		if (!isStateActionSignPayload(state)) return null;
		return (String) state.getDidState().get("kid");
	}

	public static String getStateActionSignPayloadAlg(State state) {

		if (!isStateActionSignPayload(state)) return null;
		return (String) state.getDidState().get("alg");
	}

	public static void setStateActionSignPayload(State state, String payload, String kid, String alg) {

		SetStateAction.setStateAction(state, "signPayload");
		state.getDidState().put("payload", payload);
		state.getDidState().put("kid", kid);
		state.getDidState().put("alg", alg);
	}
}
