package uniregistrar.driver.did.sov.state;

import com.fasterxml.jackson.annotation.JsonIgnore;

import uniregistrar.state.RegisterState;
import uniregistrar.state.RegisterStateAction;

public class RegisterStateActionTrustAnchor {

	private RegisterStateActionTrustAnchor() {

	}

	@JsonIgnore
	public static boolean isStateActionTrustAnchor(RegisterState registerState) {

		return "trustanchor".equals(RegisterStateAction.getStateAction(registerState));
	}

	@JsonIgnore
	public static String getStateActionTrustAnchorDid(RegisterState registerState) {

		if (! isStateActionTrustAnchor(registerState)) return null;
		return (String) registerState.getDidState().get("did");
	}

	@JsonIgnore
	public static String getStateActionTrustAnchorVerkey(RegisterState registerState) {

		if (! isStateActionTrustAnchor(registerState)) return null;
		return (String) registerState.getDidState().get("verkey");
	}

	@JsonIgnore
	public static String getStateActionTrustAnchorUrl(RegisterState registerState) {

		if (! isStateActionTrustAnchor(registerState)) return null;
		return (String) registerState.getDidState().get("url");
	}

	@JsonIgnore
	public static void setStateActionTrustAnchor(RegisterState registerState, String did, String verkey, String url) {

		RegisterStateAction.setStateAction(registerState, "trustanchor");
		registerState.getDidState().put("did", did);
		registerState.getDidState().put("verkey", verkey);
		registerState.getDidState().put("url", url);
	}
}
