package uniregistrar.state;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class SetStateActionGenerateVerificationMethod {

	private SetStateActionGenerateVerificationMethod() {

	}

	public static boolean isStateActionGenerateVerificationMethod(State state) {

		return "generateVerificationMethod".equals(SetStateAction.getStateAction(state));
	}

	public static void setStateActionGenerateVerificationMethod(State state) {

		SetStateAction.setStateAction(state, "generateVerificationMethod");
	}


	public static List<VerificationMethodTemplate> getStateActionVerificationMethodTemplates(State state) {

		if (!isStateActionGenerateVerificationMethod(state)) return null;
		List<Map<String, Object>> verificationMethodTemplates = (List<Map<String, Object>>) state.getDidState().get("verificationMethodTemplate");
		if (verificationMethodTemplates == null) return null;
		return verificationMethodTemplates
				.stream()
				.map(VerificationMethodTemplate::fromMap)
				.collect(Collectors.toList());
	}

	public static void addStateActionVerificationMethodTemplate(State state, String signingRequestId, VerificationMethodTemplate verificationMethodTemplate) {

		setStateActionGenerateVerificationMethod(state);
		List<Map<String, Object>> verificationMethodTemplates = (List<Map<String, Object>>) state.getDidState().get("verificationMethodTemplate");
		if (verificationMethodTemplates == null) {
			verificationMethodTemplates = new LinkedList<>();
			state.getDidState().put("verificationMethodTemplate", verificationMethodTemplates);
		}
		verificationMethodTemplates.add(verificationMethodTemplate.toMap());
	}
}
