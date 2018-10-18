package uniregistrar.driver.did.btcr.diddoccontinuation;

import java.io.IOException;
import java.net.URI;

import did.DIDDocument;

public interface DIDDocContinuation {

	public URI prepareDIDDocContinuation(DIDDocument didDocument);
	public void storeDIDDocContinuation(URI didContinuationUri, DIDDocument didContinuationDocument) throws IOException;
}
