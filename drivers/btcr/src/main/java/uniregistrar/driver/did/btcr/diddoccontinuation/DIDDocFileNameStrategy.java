package uniregistrar.driver.did.btcr.diddoccontinuation;

import did.DIDDocument;

public interface DIDDocFileNameStrategy {

	public String createDIDDocFileName(DIDDocument didDocument);
}
