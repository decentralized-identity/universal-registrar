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

	public static void setStateActionSignPayload(State state) {

		SetStateAction.setStateAction(state, "signPayload");
	}

	public static Map<String, SigningRequest> getStateActionSigningRequests(State state) {

		if (!isStateActionSignPayload(state)) return null;
		Map<String, Map<String, Object>> signingRequests = (Map<String, Map<String, Object>>) state.getDidState().get("signingRequest");
		if (signingRequests == null) return null;
		return signingRequests.entrySet()
				.stream()
				.collect(Collectors.toMap(map -> map.getKey(), map -> SigningRequest.fromMap(map.getValue())));
	}

	public static void addStateActionSigningRequest(State state, String signingRequestId, SigningRequest signingRequest) {

		setStateActionSignPayload(state);
		Map<String, Map<String, Object>> signingRequests = (Map<String, Map<String, Object>>) state.getDidState().get("signingRequest");
		if (signingRequests == null) {
			signingRequests = new LinkedHashMap<>();
			state.getDidState().put("signingRequest", signingRequests);
		}
		signingRequests.put(signingRequestId, signingRequest.toMap());
	}
}
