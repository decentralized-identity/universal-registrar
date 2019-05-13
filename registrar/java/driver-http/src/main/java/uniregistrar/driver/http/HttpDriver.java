package uniregistrar.driver.http;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import uniregistrar.RegistrationException;
import uniregistrar.driver.Driver;
import uniregistrar.request.RegisterRequest;
import uniregistrar.request.DeactivateRequest;
import uniregistrar.request.UpdateRequest;
import uniregistrar.state.RegisterState;
import uniregistrar.state.DeactivateState;
import uniregistrar.state.UpdateState;

public class HttpDriver implements Driver {

	private static Logger log = LoggerFactory.getLogger(HttpDriver.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();

	public static final HttpClient DEFAULT_HTTP_CLIENT = HttpClients.createDefault();
	public static final URI DEFAULT_REGISTER_URI = null;
	public static final URI DEFAULT_UPDATE_URI = null;
	public static final URI DEFAULT_DEACTIVATE_URI = null;
	public static final URI DEFAULT_PROPERTIES_URI = null;

	private HttpClient httpClient = DEFAULT_HTTP_CLIENT;
	private URI registerUri = DEFAULT_REGISTER_URI;
	private URI updateUri = DEFAULT_UPDATE_URI;
	private URI deactivateUri = DEFAULT_DEACTIVATE_URI;
	private URI propertiesUri = DEFAULT_PROPERTIES_URI;

	public HttpDriver() {

	}

	@Override
	public RegisterState register(RegisterRequest registerRequest) throws RegistrationException {

		// prepare HTTP request

		String uriString = this.getRegisterUri().toString();

		String body;

		try {

			body = registerRequest.toJson();
		} catch (JsonProcessingException ex) {

			throw new RegistrationException(ex.getMessage(), ex);
		}

		HttpPost httpPost = new HttpPost(URI.create(uriString));
		httpPost.setEntity(new StringEntity(body, ContentType.create(RegisterRequest.MIME_TYPE, StandardCharsets.UTF_8)));
		httpPost.addHeader("Accept", RegisterState.MIME_TYPE);

		// execute HTTP request

		RegisterState registerState;

		if (log.isDebugEnabled()) log.debug("Request for register request " + registerRequest + " to: " + uriString);

		try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpPost)) {

			int statusCode = httpResponse.getStatusLine().getStatusCode();
			String statusMessage = httpResponse.getStatusLine().getReasonPhrase();

			if (log.isDebugEnabled()) log.debug("Response status from " + uriString + ": " + statusCode + " " + statusMessage);

			if (statusCode == 404) return null;

			HttpEntity httpEntity = httpResponse.getEntity();
			String httpBody = EntityUtils.toString(httpEntity);
			EntityUtils.consume(httpEntity);

			if (log.isDebugEnabled()) log.debug("Response body from " + uriString + ": " + httpBody);

			if (httpResponse.getStatusLine().getStatusCode() > 200) {

				if (log.isWarnEnabled()) log.warn("Cannot retrieve REGISTER STATE for register request " + registerRequest + " from " + uriString + ": " + httpBody);
				throw new RegistrationException(httpBody);
			}

			registerState = RegisterState.fromJson(httpBody);
		} catch (IOException ex) {

			throw new RegistrationException("Cannot retrieve REGISTER STATE for register request " + registerRequest + " from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved REGISTER STATE for for register request " + registerRequest + " (" + uriString + "): " + registerState);

		// done

		return registerState;
	}

	@Override
	public UpdateState update(UpdateRequest updateRequest) throws RegistrationException {

		// prepare HTTP request

		String uriString = this.getUpdateUri().toString();

		String body;

		try {

			body = updateRequest.toJson();
		} catch (JsonProcessingException ex) {

			throw new RegistrationException(ex.getMessage(), ex);
		}

		HttpPost httpPost = new HttpPost(URI.create(uriString));
		httpPost.setEntity(new StringEntity(body, ContentType.create(UpdateRequest.MIME_TYPE, StandardCharsets.UTF_8)));
		httpPost.addHeader("Accept", UpdateState.MIME_TYPE);

		// execute HTTP request

		UpdateState updateState;

		if (log.isDebugEnabled()) log.debug("Request for update request " + updateRequest + " to: " + uriString);

		try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpPost)) {

			int statusCode = httpResponse.getStatusLine().getStatusCode();
			String statusMessage = httpResponse.getStatusLine().getReasonPhrase();

			if (log.isDebugEnabled()) log.debug("Response status from " + uriString + ": " + statusCode + " " + statusMessage);

			if (statusCode == 404) return null;

			HttpEntity httpEntity = httpResponse.getEntity();
			String httpBody = EntityUtils.toString(httpEntity);
			EntityUtils.consume(httpEntity);

			if (log.isDebugEnabled()) log.debug("Response body from " + uriString + ": " + httpBody);

			if (httpResponse.getStatusLine().getStatusCode() > 200) {

				if (log.isWarnEnabled()) log.warn("Cannot retrieve UPDATE STATE for update request " + updateRequest + " from " + uriString + ": " + httpBody);
				throw new RegistrationException(httpBody);
			}

			updateState = UpdateState.fromJson(httpBody);
		} catch (IOException ex) {

			throw new RegistrationException("Cannot retrieve UPDATE STATE for update request " + updateRequest + " from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved UPDATE STATE for for update request " + updateRequest + " (" + uriString + "): " + updateState);

		// done

		return updateState;
	}

	@Override
	public DeactivateState deactivate(DeactivateRequest deactivateRequest) throws RegistrationException {

		// prepare HTTP request

		String uriString = this.getDeactivateUri().toString();

		String body;

		try {

			body = deactivateRequest.toJson();
		} catch (JsonProcessingException ex) {

			throw new RegistrationException(ex.getMessage(), ex);
		}

		HttpPost httpPost = new HttpPost(URI.create(uriString));
		httpPost.setEntity(new StringEntity(body, ContentType.create(DeactivateRequest.MIME_TYPE, StandardCharsets.UTF_8)));
		httpPost.addHeader("Accept", DeactivateState.MIME_TYPE);

		// execute HTTP request

		DeactivateState deactivateState;

		if (log.isDebugEnabled()) log.debug("Request for deactivate request " + deactivateRequest + " to: " + uriString);

		try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpPost)) {

			int statusCode = httpResponse.getStatusLine().getStatusCode();
			String statusMessage = httpResponse.getStatusLine().getReasonPhrase();

			if (log.isDebugEnabled()) log.debug("Response status from " + uriString + ": " + statusCode + " " + statusMessage);

			if (statusCode == 404) return null;

			HttpEntity httpEntity = httpResponse.getEntity();
			String httpBody = EntityUtils.toString(httpEntity);
			EntityUtils.consume(httpEntity);

			if (log.isDebugEnabled()) log.debug("Response body from " + uriString + ": " + httpBody);

			if (httpResponse.getStatusLine().getStatusCode() > 200) {

				if (log.isWarnEnabled()) log.warn("Cannot retrieve DEACTIVATE STATE for deactivate request " + deactivateRequest + " from " + uriString + ": " + httpBody);
				throw new RegistrationException(httpBody);
			}

			deactivateState = DeactivateState.fromJson(httpBody);
		} catch (IOException ex) {

			throw new RegistrationException("Cannot retrieve DEACTIVATE STATE for deactivate request " + deactivateRequest + " from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved DEACTIVATE STATE for for deactivate request " + deactivateRequest + " (" + uriString + "): " + deactivateState);

		// done

		return deactivateState;
	}

	@Override
	public Map<String, Object> properties() throws RegistrationException {

		// prepare properties

		Map<String, Object> httpProperties = new HashMap<String, Object> ();

		if (this.getRegisterUri() != null) httpProperties.put("registerUri", this.getRegisterUri().toString());
		if (this.getUpdateUri() != null) httpProperties.put("updateUri", this.getUpdateUri().toString());
		if (this.getDeactivateUri() != null) httpProperties.put("deactivateUri", this.getDeactivateUri().toString());
		if (this.getPropertiesUri() != null) httpProperties.put("propertiesUri", this.getPropertiesUri().toString());

		Map<String, Object> properties = new HashMap<String, Object> ();
		properties.put("http", httpProperties);

		// remote properties

		Map<String, Object> remoteProperties = this.remoteProperties();
		if (remoteProperties != null) properties.putAll(remoteProperties);

		// done

		return properties;
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

			if (httpResponse.getStatusLine().getStatusCode() > 200) {

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
	 * Getters and setters
	 */

	public HttpClient getHttpClient() {

		return this.httpClient;
	}

	public void setHttpClient(HttpClient httpClient) {

		this.httpClient = httpClient;
	}

	public URI getRegisterUri() {

		return this.registerUri;
	}

	public void setRegisterUri(URI registerUri) {

		this.registerUri = registerUri;
	}

	public void setRegisterUri(String registerUri) {

		this.registerUri = URI.create(registerUri);
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
