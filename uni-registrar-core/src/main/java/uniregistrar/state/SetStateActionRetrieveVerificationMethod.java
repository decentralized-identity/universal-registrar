package uniregistrar.state;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class SetStateActionRetrieveVerificationMethod {

	private SetStateActionRetrieveVerificationMethod() {

	}

	public static boolean isStateActionRetrieveVerificationMethod(State state) {

		return "retrieveVerificationMethod".equals(SetStateAction.getStateAction(state));
	}

	public static void setStateActionRetrieveVerificationMethod(State state) {

		SetStateAction.setStateAction(state, "retrieveVerificationMethod");
	}

	public static List<VerificationMethodTemplate> getStateActionVerificationMethodTemplates(State state) {

		if (!isStateActionRetrieveVerificationMethod(state)) return null;
		List<Map<String, Object>> verificationMethodTemplates = (List<Map<String, Object>>) state.getDidState().get("verificationMethodTemplate");
		if (verificationMethodTemplates == null) return null;
		return verificationMethodTemplates
				.stream()
				.map(VerificationMethodTemplate::fromMap)
				.collect(Collectors.toList());
	}

	public static void addStateActionVerificationMethodTemplate(State state, String signingRequestId, VerificationMethodTemplate verificationMethodTemplate) {

		setStateActionRetrieveVerificationMethod(state);
		List<Map<String, Object>> verificationMethodTemplates = (List<Map<String, Object>>) state.getDidState().get("verificationMethodTemplate");
		if (verificationMethodTemplates == null) {
			verificationMethodTemplates = new LinkedList<>();
			state.getDidState().put("verificationMethodTemplate", verificationMethodTemplates);
		}
		verificationMethodTemplates.add(verificationMethodTemplate.toMap());
	}
}
