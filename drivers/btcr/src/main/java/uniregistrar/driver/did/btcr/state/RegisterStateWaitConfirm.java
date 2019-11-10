package uniregistrar.driver.did.btcr.state;

import com.fasterxml.jackson.annotation.JsonIgnore;

import uniregistrar.state.RegisterState;
import uniregistrar.state.RegisterStateWait;

public class RegisterStateWaitConfirm {

	private RegisterStateWaitConfirm() {

	}

	@JsonIgnore
	public static boolean isStateWaitConfirm(RegisterState registerState) {

		return "confirm".equals(RegisterStateWait.getStateWait(registerState));
	}

	@JsonIgnore
	public static void setStateWaitConfirm(RegisterState registerState) {

		RegisterStateWait.setStateWait(registerState, "confirm", "3600000");
	}
}
