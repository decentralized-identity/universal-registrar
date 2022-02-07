package uniregistrar.state;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class SetStateActionDecryptionRequests {

	private SetStateActionDecryptionRequests() {

	}

	public static boolean isStateActionDecryptPayload(State state) {

		return "decryptPayload".equals(SetStateAction.getStateAction(state));
	}

	public static Map<String, DecryptionRequest> getStateActionDecryptionRequests(State state) {

		if (!isStateActionDecryptPayload(state)) return null;
		Map<String, Map<String, Object>> decryptionRequests = (Map<String, Map<String, Object>>) state.getDidState().get("decryptionRequest");
		if (decryptionRequests == null) return null;
		return decryptionRequests.entrySet()
				.stream()
				.collect(Collectors.toMap(map -> map.getKey(), map -> DecryptionRequest.fromMap(map.getValue())));
	}

	public static void addStateActionDecryptionRequest(State state, String decryptionRequestId, DecryptionRequest decryptionRequest) {

		SetStateAction.setStateAction(state, "decryptPayload");
		Map<String, Map<String, Object>> decryptionRequests = (Map<String, Map<String, Object>>) state.getDidState().get("decryptionRequest");
		if (decryptionRequests == null) {
			decryptionRequests = new LinkedHashMap<>();
			state.getDidState().put("decryptionRequest", decryptionRequests);
		}
		decryptionRequests.put(decryptionRequestId, decryptionRequest.toMap());
	}
}
