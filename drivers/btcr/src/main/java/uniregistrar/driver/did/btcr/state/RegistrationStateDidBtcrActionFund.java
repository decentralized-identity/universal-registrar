package uniregistrar.driver.did.btcr.state;

import com.google.gson.JsonObject;

import uniregistrar.state.RegistrationStateAction;

public class RegistrationStateDidBtcrActionFund extends RegistrationStateAction {

	public static final String JSON_PROPERTY_BITCOINADDRESS = "bitcoinaddress";
	public static final String JSON_PROPERTY_SATOSHIS = "satoshis";

	private String bitcoinaddress;
	private String satoshis;

	public RegistrationStateDidBtcrActionFund(String jobid, String action, String bitcoinaddress, String satoshis) {

		super(jobid, "fundingrequired");

		this.bitcoinaddress = bitcoinaddress;
		this.satoshis = satoshis;
	}

	@Override
	public JsonObject toJson() {

		JsonObject jsonObject = super.toJson();

		jsonObject.addProperty(JSON_PROPERTY_ACTION, "fundingrequired");
		if (this.getBitcoinaddress() != null) jsonObject.addProperty(JSON_PROPERTY_BITCOINADDRESS, this.getBitcoinaddress());
		if (this.getSatoshis() != null) jsonObject.addProperty(JSON_PROPERTY_SATOSHIS, this.getSatoshis());

		return jsonObject;
	}

	/*
	 * Getters
	 */

	public String getBitcoinaddress() {

		return this.bitcoinaddress;
	}

	public String getSatoshis() {

		return this.satoshis;
	}
}
