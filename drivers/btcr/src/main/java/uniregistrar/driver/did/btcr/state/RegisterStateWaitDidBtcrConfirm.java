package uniregistrar.driver.did.btcr.state;

import java.util.Map;

import uniregistrar.state.RegisterStateWait;

public class RegisterStateWaitDidBtcrConfirm extends RegisterStateWait {

	public RegisterStateWaitDidBtcrConfirm(String jobid, Map<String, Object> registrarMetadata, Map<String, Object> methodMetadata) {

		super(jobid, registrarMetadata, methodMetadata, "confirmingtransaction", "3600000");
	}
}
