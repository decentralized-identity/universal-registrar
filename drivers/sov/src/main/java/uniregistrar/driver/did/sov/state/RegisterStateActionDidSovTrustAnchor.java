package uniregistrar.driver.did.sov.state;

import java.util.Map;

import uniregistrar.state.RegisterStateAction;

public class RegisterStateActionDidSovTrustAnchor extends RegisterStateAction {

	public RegisterStateActionDidSovTrustAnchor(String jobid, Map<String, Object> registrarMetadata, Map<String, Object> methodMetadata, String did, String verkey, String trustAnchorUrl) {

		super(jobid, registrarMetadata, methodMetadata, "trustanchorrequired");

		this.setDid(did);
		this.setVerkey(verkey);
		this.setTrustAnchorUril(trustAnchorUrl);
	}

	/*
	 * Getters and setters
	 */

	public final String getDid() {

		return this.getDidState() == null ? null : (String) this.getDidState().get("did");
	}

	public final void setDid(String did) {

		if (this.getDidState() != null && did != null) this.getDidState().put("did", did);
	}

	public final String getVerkey() {

		return this.getDidState() == null ? null : (String) this.getDidState().get("verkey");
	}

	public final void setVerkey(String verkey) {

		if (this.getDidState() != null && verkey != null) this.getDidState().put("verkey", verkey);
	}

	public final String getTrustAnchorUrl() {

		return this.getDidState() == null ? null : (String) this.getDidState().get("trustAnchorUrl");
	}

	public final void setTrustAnchorUril(String trustAnchorUrl) {

		if (this.getDidState() != null && trustAnchorUrl != null) this.getDidState().put("trustAnchorUrl", trustAnchorUrl);
	}
}
