package uniregistrar.driver.did.sov.state;

import com.google.gson.JsonObject;

import uniregistrar.state.RegistrationStateAction;

public class RegistrationStateDidSovActionTrustAnchor extends RegistrationStateAction {

	public static final String JSON_PROPERTY_DID = "did";
	public static final String JSON_PROPERTY_VERKEY = "verkey";
	public static final String JSON_PROPERTY_TRUSTANCHORURL = "trustanchorurl";

	private String did;
	private String verkey;
	private String trustanchorurl;

	public RegistrationStateDidSovActionTrustAnchor(String jobid, String did, String verkey, String trustanchorurl) {

		super(jobid);

		this.did = did;
		this.verkey = verkey;
		this.trustanchorurl = trustanchorurl;
	}

	@Override
	public JsonObject toJson() {

		JsonObject jsonObject = super.toJson();

		jsonObject.addProperty(JSON_PROPERTY_ACTION, "trustanchorrequired");
		if (this.getDid() != null) jsonObject.addProperty(JSON_PROPERTY_DID, this.getDid());
		if (this.getVerkey() != null) jsonObject.addProperty(JSON_PROPERTY_VERKEY, this.getVerkey());
		if (this.getTrustanchorurl() != null) jsonObject.addProperty(JSON_PROPERTY_TRUSTANCHORURL, this.getTrustanchorurl());

		return jsonObject;
	}

	/*
	 * Getters
	 */

	public String getDid() {

		return this.did;
	}

	public String getVerkey() {

		return this.verkey;
	}

	public String getTrustanchorurl() {

		return this.trustanchorurl;
	}
}
