package uniregistrar.request;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.DIDDocument;
import uniregistrar.JsonObject;

public class UpdateRequest extends JsonObject {

	public static final String MIME_TYPE = "application/json";

	private static final ObjectMapper objectMapper;

	static {
		objectMapper = new ObjectMapper().configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
	}

	@JsonProperty
	private String jobId;

	@JsonProperty
	private String did;

	@JsonProperty
	private Map<String, Object> options;

	@JsonProperty
	private Map<String, Object> secret;

	@JsonProperty
	private List<DIDDocument> didDocument;

	@JsonProperty
	private List<String> didDocumentOperation;

	public UpdateRequest() {

	}

	public UpdateRequest(String jobId, String did, Map<String, Object> options, Map<String, Object> secret, List<String> didDocumentOperation, List<DIDDocument> didDocument) {
		this.jobId = jobId;
		this.did = did;
		this.options = options;
		this.secret = secret;
		this.didDocumentOperation = didDocumentOperation;
		this.didDocument = didDocument;
	}

	/*
	 * Serialization
	 */

	public static UpdateRequest fromJson(String json) throws IOException {
		return objectMapper.readValue(json, UpdateRequest.class);
	}

	public static UpdateRequest fromJson(Reader reader) throws IOException {
		return objectMapper.readValue(reader, UpdateRequest.class);
	}

	public static UpdateRequest fromMap(Map<String, Object> map) {
		return objectMapper.convertValue(map, UpdateRequest.class);
	}

	/*
	 * Getters and setters
	 */

	@JsonGetter
	public final String getJobId() {
		return this.jobId;
	}

	@JsonSetter
	public final void setJobId(String jobId) {
		this.jobId = jobId;
	}

	@JsonGetter
	public final String getDid() {
		return this.did;
	}

	@JsonSetter
	public final void setDid(String did) {
		this.did = did;
	}

	@JsonGetter
	public final Map<String, Object> getOptions() {
		return this.options;
	}

	@JsonSetter
	public final void setOptions(Map<String, Object> options) {
		this.options = options;
	}

	@JsonGetter
	public final Map<String, Object> getSecret() {
		return this.secret;
	}

	@JsonSetter
	public final void setSecret(Map<String, Object> secret) {
		this.secret = secret;
	}

	@JsonSetter
	public void setDidDocumentOperation(List<String> didDocumentOperation) {
		this.didDocumentOperation = didDocumentOperation;
	}

	@JsonGetter
	public List<String> getDidDocumentOperation() {
		return didDocumentOperation;
	}

	@JsonSetter
	public void setDidDocument(List<DIDDocument> didDocument) {
		this.didDocument = didDocument;
	}

	@JsonGetter
	public final List<DIDDocument> getDidDocument() {
		return this.didDocument;
	}
}
