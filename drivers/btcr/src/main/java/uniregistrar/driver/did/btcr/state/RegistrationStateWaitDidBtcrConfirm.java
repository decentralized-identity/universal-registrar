package uniregistrar.driver.did.btcr.state;

import java.util.Map;

import uniregistrar.state.RegistrationStateWait;

public class RegistrationStateWaitDidBtcrConfirm extends RegistrationStateWait {

	public RegistrationStateWaitDidBtcrConfirm(String jobid, Map<String, Object> registrarMetadata) {

		super(jobid, registrarMetadata, "confirmingtransaction", "3600000");
	}

/*	@Override
	public JsonObject toJson() {

		JsonObject jsonObject = super.toJson();

		return jsonObject;
	}*/
}
