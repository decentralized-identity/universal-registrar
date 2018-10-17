package uniregistrar.driver.did.btcr.state;

import com.google.gson.JsonObject;

import uniregistrar.state.RegistrationStateWait;

public class RegistrationStateDidBtcrWaitConfirm extends RegistrationStateWait {

	public RegistrationStateDidBtcrWaitConfirm(String jobid) {

		super(jobid, "confirmingtransaction", "3600000");
	}

	@Override
	public JsonObject toJson() {

		JsonObject jsonObject = super.toJson();

		return jsonObject;
	}
}
