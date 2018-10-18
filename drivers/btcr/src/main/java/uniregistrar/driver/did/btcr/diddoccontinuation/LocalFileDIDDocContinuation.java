package uniregistrar.driver.did.btcr.diddoccontinuation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jsonldjava.core.JsonLdError;

import did.DIDDocument;

public class LocalFileDIDDocContinuation implements DIDDocContinuation {

	private static Logger log = LoggerFactory.getLogger(LocalFileDIDDocContinuation.class);

	private String basePath;
	private String baseUri;

	private DIDDocFileNameStrategy didDocFileNameStrategy = new RandomDIDDocFileNameStrategy();

	public LocalFileDIDDocContinuation(String basePath, String baseUri) {

		this.basePath = basePath;
		this.baseUri = baseUri;
	}

	public LocalFileDIDDocContinuation() {

	}

	@Override
	public URI prepareDIDDocContinuation(DIDDocument didContinuationDocument) {

		String fileName = this.getDidDocFileNameStrategy().createDIDDocFileName(didContinuationDocument);

		String fileUri = this.getBaseUri();
		if (! fileUri.endsWith("/")) fileUri += "/";
		fileUri += fileName;
		if (log.isDebugEnabled()) log.debug("DID Document continuation URI: " + fileUri);

		return URI.create(fileUri);
	}

	@Override
	public void storeDIDDocContinuation(URI didContinuationUri, DIDDocument didContinuationDocument) throws IOException {

		String fileName = didContinuationUri.toString().substring(didContinuationUri.toString().lastIndexOf("/") + 1);

		String filePath = this.getBasePath();
		if (! filePath.endsWith(File.pathSeparator)) filePath += File.pathSeparator;
		filePath += fileName;
		if (log.isDebugEnabled()) log.debug("DID Document continuation file: " + filePath);

		FileWriter fileWriter = null;

		try {

			fileWriter = new FileWriter(new File(filePath));
			fileWriter.write(didContinuationDocument.toJson());
			fileWriter.flush();
		} catch (JsonLdError ex) {

			throw new IOException("JSON-LD problem: " + ex.getMessage(), ex);
		} catch (IOException ex) {

			throw ex;
		} finally {

			if (fileWriter != null) fileWriter.close();
		}
	}

	/*
	 * Getters and setters
	 */

	public String getBasePath() {

		return this.basePath;
	}

	public void setBasePath(String basePath) {

		this.basePath = basePath;
	}

	public String getBaseUri() {

		return this.baseUri;
	}

	public void setBaseUri(String baseUri) {

		this.baseUri = baseUri;
	}

	public DIDDocFileNameStrategy getDidDocFileNameStrategy() {

		return this.didDocFileNameStrategy;
	}

	public void setDidDocFileNameStrategy(DIDDocFileNameStrategy didDocFileNameStrategy) {

		this.didDocFileNameStrategy = didDocFileNameStrategy;
	}
}
