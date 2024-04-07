package uniregistrar.driver.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpDriver implements Driver {

	private static final Logger log = LoggerFactory.getLogger(HttpDriver.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();

	public static final HttpClient DEFAULT_HTTP_CLIENT = HttpClients.createDefault();
	public static final URI DEFAULT_CREATE_URI = null;
	public static final URI DEFAULT_UPDATE_URI = null;
	public static final URI DEFAULT_DEACTIVATE_URI = null;
	public static final URI DEFAULT_PROPERTIES_URI = null;

	private HttpClient httpClient = DEFAULT_HTTP_CLIENT;
	private URI createUri = DEFAULT_CREATE_URI;
	private URI updateUri = DEFAULT_UPDATE_URI;
	private URI deactivateUri = DEFAULT_DEACTIVATE_URI;
	private URI propertiesUri = DEFAULT_PROPERTIES_URI;

	public HttpDriver() {

	}

	@Override
	public CreateState create(CreateRequest createRequest) throws RegistrationException {

		// prepare HTTP request

		String uriString = this.getCreateUri().toString();

		String body;

		try {
			body = objectMapper.writeValueAsString(createRequest);
		} catch (JsonProcessingException ex) {
			throw new RegistrationException(ex.getMessage(), ex);
		}

		HttpPost httpPost = new HttpPost(URI.create(uriString));
		httpPost.setEntity(new StringEntity(body, ContentType.create(RegistrationMediaTypes.REQUEST_MEDIA_TYPE, StandardCharsets.UTF_8)));
		httpPost.addHeader("Accept", RegistrationMediaTypes.STATE_MEDIA_TYPE);

		// execute HTTP request and read response

		CreateState createState = null;

		if (log.isDebugEnabled()) log.debug("Driver request for CREATE REQUEST " + createRequest + " to: " + uriString);

		try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpPost)) {

			// execute HTTP request

			HttpEntity httpEntity = httpResponse.getEntity();
			int httpStatusCode = httpResponse.getStatusLine().getStatusCode();
			String httpStatusMessage = httpResponse.getStatusLine().getReasonPhrase();
			ContentType httpContentType = ContentType.get(httpResponse.getEntity());
			Charset httpCharset = (httpContentType != null && httpContentType.getCharset() != null) ? httpContentType.getCharset() : HTTP.DEF_CONTENT_CHARSET;

			if (log.isDebugEnabled()) log.debug("Driver response HTTP status from " + uriString + ": " + httpStatusCode + " " + httpStatusMessage);
			if (log.isDebugEnabled()) log.debug("Driver response HTTP content type from " + uriString + ": " + httpContentType + " / " + httpCharset);

			if (httpStatusCode == 404) return null;

			// read result

			byte[] httpBodyBytes = EntityUtils.toByteArray(httpEntity);
			String httpBodyString = new String(httpBodyBytes, httpCharset);
			EntityUtils.consume(httpEntity);

			if (log.isDebugEnabled()) log.debug("Driver response HTTP body from " + uriString + ": " + httpBodyString);

			if (isStateHttpContent(httpBodyString)) {
				createState = objectMapper.readValue(httpBodyBytes, CreateState.class);
			}

			if (httpResponse.getStatusLine().getStatusCode() >= 300 && createState == null) {
				throw new RegistrationException(RegistrationException.ERROR_INTERNALERROR, "Driver cannot retrieve state: " + httpStatusCode + " " + httpStatusMessage + " (" + httpBodyString + ")");
			}

			if (createState != null && createState.getDidState() instanceof DidStateFailed didStateFailed) {
				if (log.isWarnEnabled()) log.warn(didStateFailed.getError() + " -> " + didStateFailed.getReason());
				throw new RegistrationException(createState);
			}

			if (createState == null) {
				createState = objectMapper.readValue(httpBodyString, CreateState.class);
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

		String body;

		try {
			body = objectMapper.writeValueAsString(updateRequest);
		} catch (JsonProcessingException ex) {
			throw new RegistrationException(ex.getMessage(), ex);
		}

		HttpPost httpPost = new HttpPost(URI.create(uriString));
		httpPost.setEntity(new StringEntity(body, ContentType.create(RegistrationMediaTypes.REQUEST_MEDIA_TYPE, StandardCharsets.UTF_8)));
		httpPost.addHeader("Accept", RegistrationMediaTypes.STATE_MEDIA_TYPE);

		// execute HTTP request and read response

		UpdateState updateState = null;

		if (log.isDebugEnabled()) log.debug("Driver request for UPDATE REQUEST " + updateRequest + " to: " + uriString);

		try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpPost)) {

			// execute HTTP request

			HttpEntity httpEntity = httpResponse.getEntity();
			int httpStatusCode = httpResponse.getStatusLine().getStatusCode();
			String httpStatusMessage = httpResponse.getStatusLine().getReasonPhrase();
			ContentType httpContentType = ContentType.get(httpResponse.getEntity());
			Charset httpCharset = (httpContentType != null && httpContentType.getCharset() != null) ? httpContentType.getCharset() : HTTP.DEF_CONTENT_CHARSET;

			if (log.isDebugEnabled()) log.debug("Driver response HTTP status from " + uriString + ": " + httpStatusCode + " " + httpStatusMessage);
			if (log.isDebugEnabled()) log.debug("Driver response HTTP content type from " + uriString + ": " + httpContentType + " / " + httpCharset);

			if (httpStatusCode == 404) return null;

			// read result

			byte[] httpBodyBytes = EntityUtils.toByteArray(httpEntity);
			String httpBodyString = new String(httpBodyBytes, httpCharset);
			EntityUtils.consume(httpEntity);

			if (log.isDebugEnabled()) log.debug("Driver response HTTP body from " + uriString + ": " + httpBodyString);

			if (isStateHttpContent(httpBodyString)) {
				updateState = objectMapper.readValue(httpBodyString, UpdateState.class);
			}

			if (httpResponse.getStatusLine().getStatusCode() >= 300 && updateState == null) {
				throw new RegistrationException(RegistrationException.ERROR_INTERNALERROR, "Driver cannot retrieve state: " + httpStatusCode + " " + httpStatusMessage + " (" + httpBodyString + ")");
			}

			if (updateState != null && updateState.getDidState() instanceof DidStateFailed didStateFailed) {
				if (log.isWarnEnabled()) log.warn(didStateFailed.getError() + " -> " + didStateFailed.getReason());
				throw new RegistrationException(updateState);
			}

			if (updateState == null) {
				updateState = objectMapper.readValue(httpBodyString, UpdateState.class);
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

		String body;

		try {
			body = objectMapper.writeValueAsString(deactivateRequest);
		} catch (JsonProcessingException ex) {
			throw new RegistrationException(ex.getMessage(), ex);
		}

		HttpPost httpPost = new HttpPost(URI.create(uriString));
		httpPost.setEntity(new StringEntity(body, ContentType.create(RegistrationMediaTypes.REQUEST_MEDIA_TYPE, StandardCharsets.UTF_8)));
		httpPost.addHeader("Accept", RegistrationMediaTypes.STATE_MEDIA_TYPE);

		// execute HTTP request and read response

		DeactivateState deactivateState = null;

		if (log.isDebugEnabled()) log.debug("Driver request for DEACTIVATE REQUEST " + deactivateRequest + " to: " + uriString);

		try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpPost)) {

			// execute HTTP request

			HttpEntity httpEntity = httpResponse.getEntity();
			int httpStatusCode = httpResponse.getStatusLine().getStatusCode();
			String httpStatusMessage = httpResponse.getStatusLine().getReasonPhrase();
			ContentType httpContentType = ContentType.get(httpResponse.getEntity());
			Charset httpCharset = (httpContentType != null && httpContentType.getCharset() != null) ? httpContentType.getCharset() : HTTP.DEF_CONTENT_CHARSET;

			if (log.isDebugEnabled()) log.debug("Driver response HTTP status from " + uriString + ": " + httpStatusCode + " " + httpStatusMessage);
			if (log.isDebugEnabled()) log.debug("Driver response HTTP content type from " + uriString + ": " + httpContentType + " / " + httpCharset);

			if (httpStatusCode == 404) return null;

			// read result

			byte[] httpBodyBytes = EntityUtils.toByteArray(httpEntity);
			String httpBodyString = new String(httpBodyBytes, httpCharset);
			EntityUtils.consume(httpEntity);

			if (log.isDebugEnabled()) log.debug("Driver response HTTP body from " + uriString + ": " + httpBodyString);

			if (isStateHttpContent(httpBodyString)) {
				deactivateState = objectMapper.readValue(httpBodyString, DeactivateState.class);
			}

			if (httpResponse.getStatusLine().getStatusCode() >= 300 && deactivateState == null) {
				throw new RegistrationException(RegistrationException.ERROR_INTERNALERROR, "Driver cannot retrieve state: " + httpStatusCode + " " + httpStatusMessage + " (" + httpBodyString + ")");
			}

			if (deactivateState != null && deactivateState.getDidState() instanceof DidStateFailed didStateFailed) {
				if (log.isWarnEnabled()) log.warn(didStateFailed.getError() + " -> " + didStateFailed.getReason());
				throw new RegistrationException(deactivateState);
			}

			if (deactivateState == null) {
				deactivateState = objectMapper.readValue(httpBodyString, DeactivateState.class);
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

			properties = (Map<String, Object>) objectMapper.readValue(httpBody, Map.class);
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

	public static boolean isStateHttpContent(String httpContentString) {
		try {
			Map<String, Object> json = objectMapper.readValue(httpContentString, Map.class);
			return json.containsKey("didState");
		} catch (JsonProcessingException ex) {
			return false;
		}
	}

	/*
	 * Getters and setters
	 */

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
}
