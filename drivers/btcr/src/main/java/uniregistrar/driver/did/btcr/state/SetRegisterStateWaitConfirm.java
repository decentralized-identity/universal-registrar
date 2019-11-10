package uniregistrar.driver.did.btcr.state;

import uniregistrar.state.RegisterState;
import uniregistrar.state.SetRegisterStateWait;

public class SetRegisterStateWaitConfirm {

	private SetRegisterStateWaitConfirm() {

	}

	public static boolean isStateWaitConfirm(RegisterState registerState) {

		return "confirm".equals(SetRegisterStateWait.getStateWait(registerState));
	}

	public static void setStateWaitConfirm(RegisterState registerState) {

		SetRegisterStateWait.setStateWait(registerState, "confirm", "3600000");
	}
}
