package uniregistrar.driver.did.sov.state;

import uniregistrar.state.RegisterState;
import uniregistrar.state.SetRegisterStateAction;

public class SetRegisterStateActionTrustAnchor {

	private SetRegisterStateActionTrustAnchor() {

	}

	public static boolean isStateActionTrustAnchor(RegisterState registerState) {

		return "trustanchor".equals(SetRegisterStateAction.getStateAction(registerState));
	}

	public static String getStateActionTrustAnchorDid(RegisterState registerState) {

		if (! isStateActionTrustAnchor(registerState)) return null;
		return (String) registerState.getDidState().get("did");
	}

	public static String getStateActionTrustAnchorVerkey(RegisterState registerState) {

		if (! isStateActionTrustAnchor(registerState)) return null;
		return (String) registerState.getDidState().get("verkey");
	}

	public static String getStateActionTrustAnchorUrl(RegisterState registerState) {

		if (! isStateActionTrustAnchor(registerState)) return null;
		return (String) registerState.getDidState().get("url");
	}

	public static void setStateActionTrustAnchor(RegisterState registerState, String did, String verkey, String url) {

		SetRegisterStateAction.setStateAction(registerState, "trustanchor");
		registerState.getDidState().put("did", did);
		registerState.getDidState().put("verkey", verkey);
		registerState.getDidState().put("url", url);
	}
}
