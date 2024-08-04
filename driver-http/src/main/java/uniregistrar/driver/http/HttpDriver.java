package uniregistrar.driver.http;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniregistrar.RegistrationException;
import uniregistrar.RegistrationMediaTypes;
import uniregistrar.driver.Driver;
import uniregistrar.openapi.model.*;
import uniregistrar.util.HttpBindingUtil;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class HttpDriver implements Driver {

	private static final Logger log = LoggerFactory.getLogger(HttpDriver.class);

	public static final HttpClient DEFAULT_HTTP_CLIENT = HttpClients.createDefault();
	public static final URI DEFAULT_CREATE_URI = null;
	public static final URI DEFAULT_UPDATE_URI = null;
	public static final URI DEFAULT_DEACTIVATE_URI = null;
	public static final URI DEFAULT_PROPERTIES_URI = null;
	public static final Boolean DEFAULT_INCLUDE_METHOD_PARAMETER = false;

	private HttpClient httpClient = DEFAULT_HTTP_CLIENT;
	private URI createUri = DEFAULT_CREATE_URI;
	private URI updateUri = DEFAULT_UPDATE_URI;
	private URI deactivateUri = DEFAULT_DEACTIVATE_URI;
	private URI propertiesUri = DEFAULT_PROPERTIES_URI;
	private Boolean includeMethodParameter = DEFAULT_INCLUDE_METHOD_PARAMETER;

	private String method;

	private Consumer<Map<String, Object>> beforeWriteCreateConsumer;
	private Consumer<Map<String, Object>> beforeReadCreateConsumer;
	private Consumer<Map<String, Object>> beforeWriteUpdateConsumer;
	private Consumer<Map<String, Object>> beforeReadUpdateConsumer;
	private Consumer<Map<String, Object>> beforeWriteDeactivateConsumer;
	private Consumer<Map<String, Object>> beforeReadDeactivateConsumer;

	public HttpDriver() {

	}

	@Override
	public CreateState create(CreateRequest createRequest) throws RegistrationException {

		// prepare HTTP request

		String uriString = this.getCreateUri().toString();
		if (Boolean.TRUE == this.getIncludeMethodParameter()) uriString += "?method=" + this.getMethod();

		Map<String, Object> requestMap = HttpBindingUtil.toMapRequest(createRequest);
		this.getBeforeWriteCreateConsumer().accept(requestMap);

		String httpRequestBodyString = HttpBindingUtil.toHttpBodyMap(requestMap);

		HttpPost httpPost = new HttpPost(URI.create(uriString));
		httpPost.setEntity(new StringEntity(httpRequestBodyString, ContentType.create(RegistrationMediaTypes.REQUEST_MEDIA_TYPE, StandardCharsets.UTF_8)));
		httpPost.addHeader("Accept", RegistrationMediaTypes.STATE_MEDIA_TYPE);

		// execute HTTP request and read response

		CreateState createState = null;

		if (log.isDebugEnabled()) log.debug("Driver request for CREATE REQUEST " + httpRequestBodyString + " to: " + uriString);

		try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpPost)) {

			// execute HTTP request

			HttpEntity httpResponseEntity = httpResponse.getEntity();
			int httpResponseStatusCode = httpResponse.getStatusLine().getStatusCode();
			String httpResponseStatusMessage = httpResponse.getStatusLine().getReasonPhrase();
			ContentType httpResponseContentType = ContentType.get(httpResponse.getEntity());
			Charset httpResponseCharset = (httpResponseContentType != null && httpResponseContentType.getCharset() != null) ? httpResponseContentType.getCharset() : HTTP.DEF_CONTENT_CHARSET;

			if (log.isDebugEnabled()) log.debug("Driver response HTTP status from " + uriString + ": " + httpResponseStatusCode + " " + httpResponseStatusMessage);
			if (log.isDebugEnabled()) log.debug("Driver response HTTP content type from " + uriString + ": " + httpResponseContentType + " / " + httpResponseCharset);

			if (httpResponseStatusCode == 404) return null;

			// read result

			byte[] httpResponseBodyBytes = EntityUtils.toByteArray(httpResponseEntity);
			String httpResponseBodyString = new String(httpResponseBodyBytes, httpResponseCharset);
			EntityUtils.consume(httpResponseEntity);

			if (log.isDebugEnabled()) log.debug("Driver response HTTP body from " + uriString + ": " + httpResponseBodyString);

			Map<String, Object> stateMap;
			try {
				stateMap = HttpBindingUtil.fromHttpBodyMap(httpResponseBodyString);
				this.getBeforeReadCreateConsumer().accept(stateMap);
			} catch (JsonProcessingException ex) {
				throw new RegistrationException(RegistrationException.ERROR_INTERNALERROR, "Driver cannot retrieve state: " + httpResponseStatusCode + " " + httpResponseStatusMessage + " (" + httpResponseBodyString + ")");
			}

			if (isStateHttpContent(stateMap)) {
				createState = HttpBindingUtil.fromMapState(stateMap, CreateState.class);
			}

			if (httpResponse.getStatusLine().getStatusCode() >= 300 && createState == null) {
				throw new RegistrationException(RegistrationException.ERROR_INTERNALERROR, "Driver cannot retrieve error state: " + httpResponseStatusCode + " " + httpResponseStatusMessage + " (" + httpResponseBodyString + ")");
			}

			if (createState != null && createState.getDidState() instanceof DidStateFailed didStateFailed) {
				if (log.isWarnEnabled()) log.warn(didStateFailed.getError() + " -> " + didStateFailed.getReason());
				throw new RegistrationException(createState);
			}

			if (createState == null) {
				createState = HttpBindingUtil.fromMapState(stateMap, CreateState.class);
			}
		} catch (IOException ex) {

			throw new RegistrationException("Cannot retrieve CREATE STATE for create request " + createRequest + " from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved CREATE STATE for create request " + createRequest + " (" + uriString + "): " + createState);

		// done

		return createState;
	}

	@Override
	public UpdateState update(UpdateRequest updateRequest) throws RegistrationException {

		// prepare HTTP request

		String uriString = this.getUpdateUri().toString();

		Map<String, Object> requestMap = HttpBindingUtil.toMapRequest(updateRequest);
		this.getBeforeWriteUpdateConsumer().accept(requestMap);

		String httpRequestBodyString = HttpBindingUtil.toHttpBodyMap(requestMap);

		HttpPost httpPost = new HttpPost(URI.create(uriString));
		httpPost.setEntity(new StringEntity(httpRequestBodyString, ContentType.create(RegistrationMediaTypes.REQUEST_MEDIA_TYPE, StandardCharsets.UTF_8)));
		httpPost.addHeader("Accept", RegistrationMediaTypes.STATE_MEDIA_TYPE);

		// execute HTTP request and read response

		UpdateState updateState = null;

		if (log.isDebugEnabled()) log.debug("Driver request for UPDATE REQUEST " + httpRequestBodyString + " to: " + uriString);

		try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpPost)) {

			// execute HTTP request

			HttpEntity httpResponseEntity = httpResponse.getEntity();
			int httpResponseStatusCode = httpResponse.getStatusLine().getStatusCode();
			String httpResponseStatusMessage = httpResponse.getStatusLine().getReasonPhrase();
			ContentType httpResponseContentType = ContentType.get(httpResponse.getEntity());
			Charset httpResponseCharset = (httpResponseContentType != null && httpResponseContentType.getCharset() != null) ? httpResponseContentType.getCharset() : HTTP.DEF_CONTENT_CHARSET;

			if (log.isDebugEnabled()) log.debug("Driver response HTTP status from " + uriString + ": " + httpResponseStatusCode + " " + httpResponseStatusMessage);
			if (log.isDebugEnabled()) log.debug("Driver response HTTP content type from " + uriString + ": " + httpResponseContentType + " / " + httpResponseCharset);

			if (httpResponseStatusCode == 404) return null;

			// read result

			byte[] httpResponseBodyBytes = EntityUtils.toByteArray(httpResponseEntity);
			String httpResponseBodyString = new String(httpResponseBodyBytes, httpResponseCharset);
			EntityUtils.consume(httpResponseEntity);

			if (log.isDebugEnabled()) log.debug("Driver response HTTP body from " + uriString + ": " + httpResponseBodyString);

			Map<String, Object> stateMap;
			try {
				stateMap = HttpBindingUtil.fromHttpBodyMap(httpResponseBodyString);
				this.getBeforeReadUpdateConsumer().accept(stateMap);
			} catch (JsonProcessingException ex) {
				throw new RegistrationException(RegistrationException.ERROR_INTERNALERROR, "Driver cannot retrieve state: " + httpResponseStatusCode + " " + httpResponseStatusMessage + " (" + httpResponseBodyString + ")");
			}

			if (isStateHttpContent(stateMap)) {
				updateState = HttpBindingUtil.fromMapState(stateMap, UpdateState.class);
			}

			if (httpResponse.getStatusLine().getStatusCode() >= 300 && updateState == null) {
				throw new RegistrationException(RegistrationException.ERROR_INTERNALERROR, "Driver cannot retrieve error state: " + httpResponseStatusCode + " " + httpResponseStatusMessage + " (" + httpResponseBodyString + ")");
			}

			if (updateState != null && updateState.getDidState() instanceof DidStateFailed didStateFailed) {
				if (log.isWarnEnabled()) log.warn(didStateFailed.getError() + " -> " + didStateFailed.getReason());
				throw new RegistrationException(updateState);
			}

			if (updateState == null) {
				updateState = HttpBindingUtil.fromMapState(stateMap, UpdateState.class);
			}
		} catch (IOException ex) {

			throw new RegistrationException("Cannot retrieve UPDATE STATE for update request " + updateRequest + " from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved UPDATE STATE for update request " + updateRequest + " (" + uriString + "): " + updateState);

		// done

		return updateState;
	}

	@Override
	public DeactivateState deactivate(DeactivateRequest deactivateRequest) throws RegistrationException {

		// prepare HTTP request

		String uriString = this.getDeactivateUri().toString();

		Map<String, Object> requestMap = HttpBindingUtil.toMapRequest(deactivateRequest);
		this.getBeforeWriteDeactivateConsumer().accept(requestMap);

		String httpRequestBodyString = HttpBindingUtil.toHttpBodyMap(requestMap);

		HttpPost httpPost = new HttpPost(URI.create(uriString));
		httpPost.setEntity(new StringEntity(httpRequestBodyString, ContentType.create(RegistrationMediaTypes.REQUEST_MEDIA_TYPE, StandardCharsets.UTF_8)));
		httpPost.addHeader("Accept", RegistrationMediaTypes.STATE_MEDIA_TYPE);

		// execute HTTP request and read response

		DeactivateState deactivateState = null;

		if (log.isDebugEnabled()) log.debug("Driver request for DEACTIVATE REQUEST " + httpRequestBodyString + " to: " + uriString);

		try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpPost)) {

			// execute HTTP request

			HttpEntity httpResponseEntity = httpResponse.getEntity();
			int httpResponseStatusCode = httpResponse.getStatusLine().getStatusCode();
			String httpResponseStatusMessage = httpResponse.getStatusLine().getReasonPhrase();
			ContentType httpResponseContentType = ContentType.get(httpResponse.getEntity());
			Charset httpResponseCharset = (httpResponseContentType != null && httpResponseContentType.getCharset() != null) ? httpResponseContentType.getCharset() : HTTP.DEF_CONTENT_CHARSET;

			if (log.isDebugEnabled()) log.debug("Driver response HTTP status from " + uriString + ": " + httpResponseStatusCode + " " + httpResponseStatusMessage);
			if (log.isDebugEnabled()) log.debug("Driver response HTTP content type from " + uriString + ": " + httpResponseContentType + " / " + httpResponseCharset);

			if (httpResponseStatusCode == 404) return null;

			// read result

			byte[] httpResponseBodyBytes = EntityUtils.toByteArray(httpResponseEntity);
			String httpResponseBodyString = new String(httpResponseBodyBytes, httpResponseCharset);
			EntityUtils.consume(httpResponseEntity);

			if (log.isDebugEnabled()) log.debug("Driver response HTTP body from " + uriString + ": " + httpResponseBodyString);

			Map<String, Object> stateMap;
			try {
				stateMap = HttpBindingUtil.fromHttpBodyMap(httpResponseBodyString);
				this.getBeforeReadDeactivateConsumer().accept(stateMap);
			} catch (JsonProcessingException ex) {
				throw new RegistrationException(RegistrationException.ERROR_INTERNALERROR, "Driver cannot retrieve state: " + httpResponseStatusCode + " " + httpResponseStatusMessage + " (" + httpResponseBodyString + ")");
			}

			if (isStateHttpContent(stateMap)) {
				deactivateState = HttpBindingUtil.fromMapState(stateMap, DeactivateState.class);
			}

			if (httpResponse.getStatusLine().getStatusCode() >= 300 && deactivateState == null) {
				throw new RegistrationException(RegistrationException.ERROR_INTERNALERROR, "Driver cannot retrieve error state: " + httpResponseStatusCode + " " + httpResponseStatusMessage + " (" + httpResponseBodyString + ")");
			}

			if (deactivateState != null && deactivateState.getDidState() instanceof DidStateFailed didStateFailed) {
				if (log.isWarnEnabled()) log.warn(didStateFailed.getError() + " -> " + didStateFailed.getReason());
				throw new RegistrationException(deactivateState);
			}

			if (deactivateState == null) {
				deactivateState = HttpBindingUtil.fromMapState(stateMap, DeactivateState.class);
			}
		} catch (IOException ex) {

			throw new RegistrationException("Cannot retrieve DEACTIVATE STATE for deactivate request " + deactivateRequest + " from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved DEACTIVATE STATE for deactivate request " + deactivateRequest + " (" + uriString + "): " + deactivateState);

		// done

		return deactivateState;
	}

	@Override
	public Map<String, Object> properties() throws RegistrationException {

		// prepare properties

		Map<String, Object> httpProperties = getHttpProperties();

		Map<String, Object> properties = new HashMap<>();
		properties.put("http", httpProperties);

		// remote properties

		try {

			Map<String, Object> remoteProperties = this.remoteProperties();
			if (remoteProperties != null) properties.putAll(remoteProperties);
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Cannot retrieve remote properties: " + ex.getMessage(), ex);
			properties.put("remotePropertiesException", ex.getMessage());
		}

		// done

		return properties;
	}

	private Map<String, Object> getHttpProperties() {
		Map<String, Object> httpProperties = new HashMap<>();

		if (this.getCreateUri() != null) httpProperties.put("createUri", this.getCreateUri().toString());
		if (this.getUpdateUri() != null) httpProperties.put("updateUri", this.getUpdateUri().toString());
		if (this.getDeactivateUri() != null) httpProperties.put("deactivateUri", this.getDeactivateUri().toString());
		if (this.getPropertiesUri() != null) httpProperties.put("propertiesUri", this.getPropertiesUri().toString());
		if (this.getIncludeMethodParameter() != null) httpProperties.put("propertiesUri", Boolean.toString(this.getIncludeMethodParameter()));
		return httpProperties;
	}

	public Map<String, Object> remoteProperties() throws RegistrationException {

		if (this.getPropertiesUri() == null) return null;

		// prepare HTTP request

		String uriString = this.getPropertiesUri().toString();

		HttpGet httpGet = new HttpGet(URI.create(uriString));
		httpGet.addHeader("Accept", Driver.PROPERTIES_MIME_TYPE);

		// execute HTTP request

		Map<String, Object> properties;

		if (log.isDebugEnabled()) log.debug("Request to: " + uriString);

		try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpGet)) {

			int statusCode = httpResponse.getStatusLine().getStatusCode();
			String statusMessage = httpResponse.getStatusLine().getReasonPhrase();

			if (log.isDebugEnabled()) log.debug("Response status from " + uriString + ": " + statusCode + " " + statusMessage);

			if (statusCode == 404) return null;

			HttpEntity httpEntity = httpResponse.getEntity();
			String httpBody = EntityUtils.toString(httpEntity);
			EntityUtils.consume(httpEntity);

			if (log.isDebugEnabled()) log.debug("Response body from " + uriString + ": " + httpBody);

			if (httpResponse.getStatusLine().getStatusCode() >= 300) {

				if (log.isWarnEnabled()) log.warn("Cannot retrieve DRIVER PROPERTIES from " + uriString + ": " + httpBody);
				throw new RegistrationException(httpBody);
			}

			properties = (Map<String, Object>) HttpBindingUtil.fromHttpBodyMap(httpBody);
		} catch (IOException ex) {

			throw new RegistrationException("Cannot retrieve DRIVER PROPERTIES from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved DRIVER PROPERTIES (" + uriString + "): " + properties);

		// done

		return properties;
	}

	/*
	 * Helper methods
	 */

	public static boolean isStateHttpContent(Map<String, Object> httpContentJson) {
		return httpContentJson.containsKey("didState");
	}

	/*
	 * Getters and setters
	 */

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public HttpClient getHttpClient() {
		return this.httpClient;
	}

	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public URI getCreateUri() {
		return this.createUri;
	}

	public void setCreateUri(URI createUri) {
		this.createUri = createUri;
	}

	public void setCreateUri(String createUri) {
		this.createUri = URI.create(createUri);
	}

	public URI getUpdateUri() {
		return this.updateUri;
	}

	public void setUpdateUri(URI updateUri) {
		this.updateUri = updateUri;
	}

	public void setUpdateUri(String updateUri) {

		this.updateUri = URI.create(updateUri);
	}

	public URI getDeactivateUri() {
		return this.deactivateUri;
	}

	public void setDeactivateUri(URI deactivateUri) {
		this.deactivateUri = deactivateUri;
	}

	public void setDeactivateUri(String deactivateUri) {
		this.deactivateUri = URI.create(deactivateUri);
	}

	public URI getPropertiesUri() {
		return this.propertiesUri;
	}

	public void setPropertiesUri(URI propertiesUri) {
		this.propertiesUri = propertiesUri;
	}

	public void setPropertiesUri(String propertiesUri) {
		this.propertiesUri = URI.create(propertiesUri);
	}

	public Boolean getIncludeMethodParameter() {
		return includeMethodParameter;
	}

	public void setIncludeMethodParameter(Boolean includeMethodParameter) {
		this.includeMethodParameter = includeMethodParameter;
	}

	public Consumer<Map<String, Object>> getBeforeWriteCreateConsumer() {
		return beforeWriteCreateConsumer;
	}

	public void setBeforeWriteCreateConsumer(Consumer<Map<String, Object>> beforeWriteCreateConsumer) {
		this.beforeWriteCreateConsumer = beforeWriteCreateConsumer;
	}

	public Consumer<Map<String, Object>> getBeforeReadCreateConsumer() {
		return beforeReadCreateConsumer;
	}

	public void setBeforeReadCreateConsumer(Consumer<Map<String, Object>> beforeReadCreateConsumer) {
		this.beforeReadCreateConsumer = beforeReadCreateConsumer;
	}

	public Consumer<Map<String, Object>> getBeforeWriteUpdateConsumer() {
		return beforeWriteUpdateConsumer;
	}

	public void setBeforeWriteUpdateConsumer(Consumer<Map<String, Object>> beforeWriteUpdateConsumer) {
		this.beforeWriteUpdateConsumer = beforeWriteUpdateConsumer;
	}

	public Consumer<Map<String, Object>> getBeforeReadUpdateConsumer() {
		return beforeReadUpdateConsumer;
	}

	public void setBeforeReadUpdateConsumer(Consumer<Map<String, Object>> beforeReadUpdateConsumer) {
		this.beforeReadUpdateConsumer = beforeReadUpdateConsumer;
	}

	public Consumer<Map<String, Object>> getBeforeWriteDeactivateConsumer() {
		return beforeWriteDeactivateConsumer;
	}

	public void setBeforeWriteDeactivateConsumer(Consumer<Map<String, Object>> beforeWriteDeactivateConsumer) {
		this.beforeWriteDeactivateConsumer = beforeWriteDeactivateConsumer;
	}

	public Consumer<Map<String, Object>> getBeforeReadDeactivateConsumer() {
		return beforeReadDeactivateConsumer;
	}

	public void setBeforeReadDeactivateConsumer(Consumer<Map<String, Object>> beforeReadDeactivateConsumer) {
		this.beforeReadDeactivateConsumer = beforeReadDeactivateConsumer;
	}
}
