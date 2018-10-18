package uniregistrar.driver.did.btcr.diddoccontinuation;

import java.util.UUID;

import did.DIDDocument;

public class RandomDIDDocFileNameStrategy implements DIDDocFileNameStrategy {

	@Override
	public String createDIDDocFileName(DIDDocument didDocument) {

		return UUID.randomUUID().toString();
	}
}
