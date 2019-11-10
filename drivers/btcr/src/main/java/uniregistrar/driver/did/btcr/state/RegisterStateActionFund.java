package uniregistrar.driver.did.btcr.state;

import com.fasterxml.jackson.annotation.JsonIgnore;

import uniregistrar.state.RegisterState;
import uniregistrar.state.RegisterStateAction;

public class RegisterStateActionFund {

	private RegisterStateActionFund() {

	}

	@JsonIgnore
	public static boolean isStateActionFund(RegisterState registerState) {

		return "fund".equals(RegisterStateAction.getStateAction(registerState));
	}

	@JsonIgnore
	public static String getStateActionFundBitcoinAddress(RegisterState registerState) {

		if (! isStateActionFund(registerState)) return null;
		return (String) registerState.getDidState().get("bitcoinAddress");
	}

	@JsonIgnore
	public static String getStateActionFundSatoshis(RegisterState registerState) {

		if (! isStateActionFund(registerState)) return null;
		return (String) registerState.getDidState().get("satoshis");
	}

	@JsonIgnore
	public static void setStateActionFund(RegisterState registerState, String bitcoinAddress, String satoshis) {

		RegisterStateAction.setStateAction(registerState, "fund");
		registerState.getDidState().put("bitcoinAddress", bitcoinAddress);
		registerState.getDidState().put("satoshis", satoshis);
	}
}
