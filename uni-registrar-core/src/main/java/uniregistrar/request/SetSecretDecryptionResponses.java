package uniregistrar.request;

import uniregistrar.state.State;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class SetSecretDecryptionResponses {

	private SetSecretDecryptionResponses() {

	}

	public static Map<String, DecryptionResponse> getSecretDecryptionResponses(State state) {

		Map<String, Map<String, Object>> decryptionResponses = (Map<String, Map<String, Object>>) state.getDidState().get("decryptionResponse");
		if (decryptionResponses == null) return null;
		return decryptionResponses.entrySet()
				.stream()
				.collect(Collectors.toMap(map -> map.getKey(), map -> DecryptionResponse.fromMap(map.getValue())));
	}

	public static void addSecretDecryptionResponse(State state, String decryptionRequestId, DecryptionResponse decryptionResponse) {

		Map<String, Map<String, Object>> decryptionResponses = (Map<String, Map<String, Object>>) state.getDidState().get("decryptionResponse");
		if (decryptionResponses == null) {
			decryptionResponses = new LinkedHashMap<>();
			state.getDidState().put("decryptionResponse", decryptionResponses);
		}
		decryptionResponses.put(decryptionRequestId, decryptionResponse.toMap());
	}
}
