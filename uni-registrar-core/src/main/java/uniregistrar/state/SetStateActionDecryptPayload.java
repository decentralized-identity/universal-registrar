package uniregistrar.state;

import java.util.Map;

public final class SetStateActionDecryptPayload {

	private SetStateActionDecryptPayload() {

	}

	public static boolean isStateActionDecryptPayload(State state) {

		return "decryptPayload".equals(SetStateAction.getStateAction(state));
	}

	public static String getStateActionDecryptPayloadKid(State state) {

		if (!isStateActionDecryptPayload(state)) return null;
		return (String) state.getDidState().get("kid");
	}

	public static String getStateActionDecryptPayloadAlg(State state) {

		if (!isStateActionDecryptPayload(state)) return null;
		return (String) state.getDidState().get("alg");
	}

	public static Map<String, Object> getStateActionDecryptPayloadPayload(State state) {

		if (!isStateActionDecryptPayload(state)) return null;
		return (Map<String, Object>) state.getDidState().get("payload");
	}

	public static String getStateActionDecryptPayloadEncryptedPayload(State state) {

		if (!isStateActionDecryptPayload(state)) return null;
		return (String) state.getDidState().get("encryptedPayload");
	}

	public static void setStateActionDecryptPayload(State state, String kid, String alg, Map<String, Object> payload, String encryptedPayload) {

		SetStateAction.setStateAction(state, "decryptPayload");
		if (kid != null) state.getDidState().put("kid", kid);
		if (alg != null) state.getDidState().put("alg", alg);
		if (payload != null) state.getDidState().put("payload", payload);
		if (encryptedPayload != null) state.getDidState().put("encryptedPayload", encryptedPayload);
	}
}
