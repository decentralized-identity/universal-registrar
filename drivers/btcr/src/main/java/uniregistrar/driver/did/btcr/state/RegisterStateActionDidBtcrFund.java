package uniregistrar.driver.did.btcr.state;

import java.util.Map;

import uniregistrar.state.RegisterStateAction;

public class RegisterStateActionDidBtcrFund extends RegisterStateAction {

	public RegisterStateActionDidBtcrFund(String jobid, Map<String, Object> registrarMetadata, Map<String, Object> methodMetadata, String bitcoinAddress, String satoshis) {

		super(jobid, registrarMetadata, methodMetadata, "fundingrequired");

		this.setBitcoinAddress(bitcoinAddress);
		this.setSatoshis(satoshis);
	}

	/*
	 * Getters and setters
	 */

	public final String getBitcoinAddress() {

		return this.getDidState() == null ? null : (String) this.getDidState().get("bitcoinAddress");
	}

	public final void setBitcoinAddress(String bitcoinAddress) {

		if (this.getDidState() != null && bitcoinAddress != null) this.getDidState().put("bitcoinAddress", bitcoinAddress);
	}

	public final String getSatoshis() {

		return this.getDidState() == null ? null : (String) this.getDidState().get("satoshis");
	}

	public final void setSatoshis(String satoshis) {

		if (this.getDidState() != null && satoshis != null) this.getDidState().put("satoshis", satoshis);
	}
}
