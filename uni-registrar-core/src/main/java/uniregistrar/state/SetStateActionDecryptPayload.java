package uniregistrar.state;

import java.util.Map;

public final class SetStateActionSignPayload {

	private SetStateActionSignPayload() {

	}

	public static boolean isStateActionSignPayload(State state) {

		return "signPayload".equals(SetStateAction.getStateAction(state));
	}

	public static String getStateActionSignPayloadKid(State state) {

		if (!isStateActionSignPayload(state)) return null;
		return (String) state.getDidState().get("kid");
	}

	public static String getStateActionSignPayloadAlg(State state) {

		if (!isStateActionSignPayload(state)) return null;
		return (String) state.getDidState().get("alg");
	}

	public static Map<String, Object> getStateActionSignPayloadPayload(State state) {

		if (!isStateActionSignPayload(state)) return null;
		return (Map<String, Object>) state.getDidState().get("payload");
	}

	public static String getStateActionSignPayloadSerializedPayload(State state) {

		if (!isStateActionSignPayload(state)) return null;
		return (String) state.getDidState().get("serializedPayload");
	}

	public static String getStateActionSignPayloadProofPurpose(State state) {

		if (!isStateActionSignPayload(state)) return null;
		return (String) state.getDidState().get("proofPurpose");
	}

	public static void setStateActionSignPayload(State state, String kid, String alg, Map<String, Object> payload, String serializedPayload, String proofPurpose) {

		SetStateAction.setStateAction(state, "signPayload");
		state.getDidState().put("kid", kid);
		state.getDidState().put("alg", alg);
		state.getDidState().put("payload", payload);
		state.getDidState().put("serializedPayload", serializedPayload);
		state.getDidState().put("proofPurpose", proofPurpose);
	}
}
