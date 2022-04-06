package uniregistrar.state;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class SetStateActionGetVerificationMethod {

	private SetStateActionGetVerificationMethod() {

	}

	public static boolean isStateActionGetVerificationMethod(State state) {

		return "getVerificationMethod".equals(SetStateAction.getStateAction(state));
	}

	public static void setStateActionGetVerificationMethod(State state) {

		SetStateAction.setStateAction(state, "getVerificationMethod");
	}


	public static List<VerificationMethodTemplate> getStateActionVerificationMethodTemplates(State state) {

		if (!isStateActionGetVerificationMethod(state)) return null;
		List<Map<String, Object>> verificationMethodTemplates = (List<Map<String, Object>>) state.getDidState().get("verificationMethodTemplate");
		if (verificationMethodTemplates == null) return null;
		return verificationMethodTemplates
				.stream()
				.map(VerificationMethodTemplate::fromMap)
				.collect(Collectors.toList());
	}

	public static void addStateActionVerificationMethodTemplate(State state, String signingRequestId, VerificationMethodTemplate verificationMethodTemplate) {

		setStateActionGetVerificationMethod(state);
		List<Map<String, Object>> verificationMethodTemplates = (List<Map<String, Object>>) state.getDidState().get("verificationMethodTemplate");
		if (verificationMethodTemplates == null) {
			verificationMethodTemplates = new LinkedList<>();
			state.getDidState().put("verificationMethodTemplate", verificationMethodTemplates);
		}
		verificationMethodTemplates.add(verificationMethodTemplate.toMap());
	}
}
