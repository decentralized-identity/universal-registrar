package uniregistrar.request;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class SetSecretSigningResponses {

	private SetSecretSigningResponses() {

	}

	public static Map<String, SigningResponse> getSecretSigningResponses(Map<String, Object> secret) {

		Map<String, Map<String, Object>> signingResponses = (Map<String, Map<String, Object>>) secret.get("signingResponse");
		if (signingResponses == null) return null;
		return signingResponses.entrySet()
				.stream()
				.collect(Collectors.toMap(map -> map.getKey(), map -> SigningResponse.fromMap(map.getValue())));
	}

	public static void addSecretSigningResponse(Map<String, Object> secret, String signingRequestId, SigningResponse signingResponse) {

		Map<String, Map<String, Object>> signingResponses = (Map<String, Map<String, Object>>) secret.get("signingResponse");
		if (signingResponses == null) {
			signingResponses = new LinkedHashMap<>();
			secret.put("signingResponse", signingResponses);
		}
		signingResponses.put(signingRequestId, signingResponse.toMap());
	}
}
