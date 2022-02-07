package uniregistrar.state;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

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

	public static Map<String, SigningRequest> getStateActionSignPayloadSigningRequests(State state) {

		if (!isStateActionSignPayload(state)) return null;
		Map<String, Map<String, Object>> signingRequests = (Map<String, Map<String, Object>>) state.getDidState().get("signingRequest");
		if (signingRequests == null) return null;
		return signingRequests.entrySet()
				.stream()
				.collect(Collectors.toMap(map -> map.getKey(), map -> SigningRequest.fromMap(map.getValue())));
	}

	public static Map<String, DecryptionRequest> getStateActionSignPayloadDecryptionRequests(State state) {

		if (!isStateActionSignPayload(state)) return null;
		Map<String, Map<String, Object>> decryptionRequests = (Map<String, Map<String, Object>>) state.getDidState().get("decryptionRequest");
		if (decryptionRequests == null) return null;
		return decryptionRequests.entrySet()
				.stream()
				.collect(Collectors.toMap(map -> map.getKey(), map -> DecryptionRequest.fromMap(map.getValue())));
	}

	public static void addStateActionSignPayload(State state, String signingRequestId, SigningRequest signingRequest) {

		SetStateAction.setStateAction(state, "signPayload");
		Map<String, Map<String, Object>> signingRequests = (Map<String, Map<String, Object>>) state.getDidState().get("signingRequest");
		if (signingRequests == null) {
			signingRequests = new LinkedHashMap<>();
			state.getDidState().put("signingRequest", signingRequests);
		}
		signingRequests.put(signingRequestId, signingRequest.toMap());
	}

	public static void addStateActionDecryptPayload(State state, String decryptionRequestId, DecryptionRequest decryptionRequest) {

		SetStateAction.setStateAction(state, "decryptPayload");
		Map<String, Map<String, Object>> decryptionRequests = (Map<String, Map<String, Object>>) state.getDidState().get("decryptionRequest");
		if (decryptionRequests == null) {
			decryptionRequests = new LinkedHashMap<>();
			state.getDidState().put("decryptionRequest", decryptionRequests);
		}
		decryptionRequests.put(decryptionRequestId, decryptionRequest.toMap());
	}
}
