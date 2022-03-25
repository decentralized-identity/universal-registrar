package uniregistrar.request;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.DIDDocument;

public class UpdateRequest {

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

	public UpdateRequest(String jobId, String did, Map<String, Object> options, Map<String, Object> secret, DIDDocument didDocument) {
		this.jobId = jobId;
		this.did = did;
		this.options = options;
		this.secret = secret;
		this.didDocument = new ArrayList<>(Arrays.asList(didDocument));
	}

	public UpdateRequest(String jobId, String did, Map<String, Object> options, Map<String, Object> secret, List<String> didDocumentOperation, List<DIDDocument> didDocuments) {
		this.jobId = jobId;
		this.did = did;
		this.options = options;
		this.secret = secret;
		this.didDocumentOperation = didDocumentOperation;
		this.didDocument = didDocuments;
	}

	/*
	 * Serialization
	 */

	public static UpdateRequest fromJson(String json) throws JsonParseException, JsonMappingException, IOException {
		return objectMapper.readValue(json, UpdateRequest.class);
	}

	public static UpdateRequest fromJson(Reader reader) throws JsonParseException, JsonMappingException, IOException {
		return objectMapper.readValue(reader, UpdateRequest.class);
	}

	public String toJson() throws JsonProcessingException {
		return objectMapper.writeValueAsString(this);
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

	public List<String> getDidDocumentOperation() {
		return didDocumentOperation;
	}

	@JsonSetter(value = "didDocument")
	public void setDidDocument(List<DIDDocument> didDocument) {
		this.didDocument = didDocument;
	}

	@Deprecated(since = "0.3", forRemoval = true)
	public final void setDidDocument(DIDDocument didDocument) {
		this.didDocument = List.of(didDocument);
	}

	@Deprecated(since = "0.3", forRemoval = true)
	public final DIDDocument getDidDocument() {
		if(this.didDocument == null || this.didDocument.isEmpty()) return null;
		if(this.didDocument.size() > 1) throw new IllegalStateException("Error: There are " + this.didDocument.size() + " DIDDocuments in the List. Use getDidDocuments instead.");
		return this.didDocument.get(0);
	}
	@JsonGetter(value = "didDocument")
	public final List<DIDDocument> getDidDocuments() {
		return this.didDocument;
	}


	/*
	 * Object methods
	 */

	public Map<String, Object> toMap(){
		return objectMapper.convertValue(this, new TypeReference<Map<String, Object>>(){});
	}

	public String toString() {

		try {

			return this.toJson();
		} catch (JsonProcessingException ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}
	}
}
