package uniregistrar.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
import uniregistrar.UniRegistrar;
import uniregistrar.request.CreateRequest;
import uniregistrar.request.DeactivateRequest;
import uniregistrar.request.UpdateRequest;
import uniregistrar.state.CreateState;
import uniregistrar.state.DeactivateState;
import uniregistrar.state.UpdateState;

public class ClientUniRegistrar implements UniRegistrar {

	private static Logger log = LoggerFactory.getLogger(ClientUniRegistrar.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();

	public static final HttpClient DEFAULT_HTTP_CLIENT = HttpClients.createDefault();
	public static final URI DEFAULT_CREATE_URI = URI.create("http://localhost:8080/1.0/create");
	public static final URI DEFAULT_UPDATE_URI = URI.create("http://localhost:8080/1.0/update");
	public static final URI DEFAULT_DEACTIVATE_URI = URI.create("http://localhost:8080/1.0/deactivate");
	public static final URI DEFAULT_PROPERTIES_URI = URI.create("http://localhost:8080/1.0/properties");

	private HttpClient httpClient = DEFAULT_HTTP_CLIENT;
	private URI createUri = DEFAULT_CREATE_URI;
	private URI updateUri = DEFAULT_UPDATE_URI;
	private URI deactivateUri = DEFAULT_DEACTIVATE_URI;
	private URI propertiesUri = DEFAULT_PROPERTIES_URI;

	public ClientUniRegistrar() {

	}

	@Override
	public CreateState create(String driverId, CreateRequest createRequest) throws RegistrationException {

		if (driverId == null) throw new NullPointerException();
		if (createRequest == null) throw new NullPointerException();

		// prepare HTTP request

		String uriString = this.getCreateUri().toString() + "?driverId=" + urlEncode(driverId);

		String body;

		try {

			body = createRequest.toJson();
		} catch (JsonProcessingException ex) {

			throw new RegistrationException(ex.getMessage(), ex);
		}

		HttpPost httpPost = new HttpPost(URI.create(uriString));
		httpPost.setEntity(new StringEntity(body, ContentType.create(CreateRequest.MIME_TYPE, StandardCharsets.UTF_8)));
		httpPost.addHeader("Accept", CreateState.MIME_TYPE);

		// execute HTTP request

		CreateState createState;

		if (log.isDebugEnabled()) log.debug("Request for create request " + createRequest + " to: " + uriString);

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

				if (log.isWarnEnabled()) log.warn("Cannot retrieve CREATE STATE for create request " + createRequest + " from " + uriString + ": " + httpBody);
				throw new RegistrationException(httpBody);
			}

			createState = CreateState.fromJson(httpBody);
		} catch (IOException ex) {

			throw new RegistrationException("Cannot retrieve CREATE STATE for create request " + createRequest + " from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved CREATE STATE for for create request " + createRequest + " (" + uriString + "): " + createState);

		// done

		return createState;
	}

	@Override
	public UpdateState update(String driverId, UpdateRequest updateRequest) throws RegistrationException {

		if (driverId == null) throw new NullPointerException();
		if (updateRequest == null) throw new NullPointerException();

		// prepare HTTP request

		String uriString = this.getUpdateUri().toString() + "?driverId=" + urlEncode(driverId);

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
	public DeactivateState deactivate(String driverId, DeactivateRequest deactivateRequest) throws RegistrationException {

		if (driverId == null) throw new NullPointerException();
		if (deactivateRequest == null) throw new NullPointerException();

		// prepare HTTP request

		String uriString = this.getDeactivateUri().toString() + "?driverId=" + urlEncode(driverId);

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
	public Map<String, Map<String, Object>> properties() throws RegistrationException {

		// prepare HTTP request

		String uriString = this.getPropertiesUri().toString();

		HttpGet httpGet = new HttpGet(URI.create(uriString));
		httpGet.addHeader("Accept", UniRegistrar.PROPERTIES_MIME_TYPE);

		// execute HTTP request

		Map<String, Map<String, Object>> properties;

		if (log.isDebugEnabled()) log.debug("Request to: " + uriString);

		try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpGet)) {

			int statusCode = httpResponse.getStatusLine().getStatusCode();
			String statusMessage = httpResponse.getStatusLine().getReasonPhrase();

			if (log.isDebugEnabled()) log.debug("Response status from " + uriString + ": " + statusCode + " " + statusMessage);

			if (httpResponse.getStatusLine().getStatusCode() == 404) return null;

			HttpEntity httpEntity = httpResponse.getEntity();
			String httpBody = EntityUtils.toString(httpEntity);
			EntityUtils.consume(httpEntity);

			if (log.isDebugEnabled()) log.debug("Response body from " + uriString + ": " + httpBody);

			if (httpResponse.getStatusLine().getStatusCode() > 200) {

				if (log.isWarnEnabled()) log.warn("Cannot retrieve DRIVER PROPERTIES from " + uriString + ": " + httpBody);
				throw new RegistrationException(httpBody);
			}

			properties = (Map<String, Map<String, Object>>) objectMapper.readValue(httpBody, Map.class);
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

	private static String urlEncode(String string) {

		try {

			return URLEncoder.encode(string, "UTF-8");
		} catch (UnsupportedEncodingException ex) {

			throw new RuntimeException(ex.getMessage(), ex);
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
