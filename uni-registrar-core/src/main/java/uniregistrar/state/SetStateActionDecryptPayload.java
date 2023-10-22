package uniregistrar.state;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class SetStateActionDecryptPayload {

	private SetStateActionDecryptPayload() {

	}

	public static boolean isStateActionDecryptPayload(State state) {

		return "decryptPayload".equals(SetStateAction.getStateAction(state));
	}

	public static void setStateActionDecryptPayload(State state) {

		SetStateAction.setStateAction(state, "decryptPayload");
	}

	public static Map<String, DecryptionRequest> getStateActionDecryptionRequests(State state) {

		if (!isStateActionDecryptPayload(state)) return null;
		Map<String, Map<String, Object>> decryptionRequests = (Map<String, Map<String, Object>>) state.getDidState().get("decryptionRequest");
		if (decryptionRequests == null) return null;
		return decryptionRequests.entrySet()
				.stream()
				.collect(Collectors.toMap(Map.Entry::getKey, map -> DecryptionRequest.fromMap(map.getValue())));
	}

	public static void addStateActionDecryptionRequest(State state, String decryptionRequestId, DecryptionRequest decryptionRequest) {

		setStateActionDecryptPayload(state);
		Map<String, Map<String, Object>> decryptionRequests = (Map<String, Map<String, Object>>) state.getDidState().get("decryptionRequest");
		if (decryptionRequests == null) {
			decryptionRequests = new LinkedHashMap<>();
			state.getDidState().put("decryptionRequest", decryptionRequests);
		}
		decryptionRequests.put(decryptionRequestId, decryptionRequest.toMap());
	}
}
