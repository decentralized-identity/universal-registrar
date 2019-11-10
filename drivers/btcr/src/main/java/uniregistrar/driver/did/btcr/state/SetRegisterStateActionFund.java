package uniregistrar.driver.did.btcr.state;

import uniregistrar.state.RegisterState;
import uniregistrar.state.SetRegisterStateAction;

public class SetRegisterStateActionFund {

	private SetRegisterStateActionFund() {

	}

	public static boolean isStateActionFund(RegisterState registerState) {

		return "fund".equals(SetRegisterStateAction.getStateAction(registerState));
	}

	public static String getStateActionFundBitcoinAddress(RegisterState registerState) {

		if (! isStateActionFund(registerState)) return null;
		return (String) registerState.getDidState().get("bitcoinAddress");
	}

	public static String getStateActionFundSatoshis(RegisterState registerState) {

		if (! isStateActionFund(registerState)) return null;
		return (String) registerState.getDidState().get("satoshis");
	}

	public static void setStateActionFund(RegisterState registerState, String bitcoinAddress, String satoshis) {

		SetRegisterStateAction.setStateAction(registerState, "fund");
		registerState.getDidState().put("bitcoinAddress", bitcoinAddress);
		registerState.getDidState().put("satoshis", satoshis);
	}
}
