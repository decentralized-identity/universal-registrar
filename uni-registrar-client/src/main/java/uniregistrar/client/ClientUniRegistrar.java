package uniregistrar.client;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uniregistrar.RegistrationException;
import uniregistrar.RegistrationMediaTypes;
import uniregistrar.UniRegistrar;
import uniregistrar.openapi.model.*;
import uniregistrar.util.HttpBindingUtil;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ClientUniRegistrar implements UniRegistrar {

	private static final Logger log = LoggerFactory.getLogger(ClientUniRegistrar.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();

	public static final HttpClient DEFAULT_HTTP_CLIENT = HttpClients.createDefault();
	public static final Map<String, String> DEFAULT_HTTP_HEADERS = Collections.emptyMap();
	public static final URI DEFAULT_CREATE_URI = URI.create("http://localhost:9080/1.0/create");
	public static final URI DEFAULT_UPDATE_URI = URI.create("http://localhost:9080/1.0/update");
	public static final URI DEFAULT_DEACTIVATE_URI = URI.create("http://localhost:9080/1.0/deactivate");
	public static final URI DEFAULT_EXECUTE_URI = URI.create("http://localhost:9080/1.0/execute");
	public static final URI DEFAULT_CREATE_RESOURCE_URI = URI.create("http://localhost:9080/1.0/createResource");
	public static final URI DEFAULT_UPDATE_RESOURCE_URI = URI.create("http://localhost:9080/1.0/updateResource");
	public static final URI DEFAULT_DEACTIVATE_RESOURCE_URI = URI.create("http://localhost:9080/1.0/deactivateResource");
	public static final URI DEFAULT_PROPERTIES_URI = URI.create("http://localhost:9080/1.0/properties");
	public static final URI DEFAULT_METHODS_URI = URI.create("http://localhost:9080/1.0/methods");
	public static final URI DEFAULT_TRAITS_URI = URI.create("http://localhost:9080/1.0/traits");

	private HttpClient httpClient = DEFAULT_HTTP_CLIENT;
	private Map<String, String> httpHeaders = DEFAULT_HTTP_HEADERS;
	private URI createUri = DEFAULT_CREATE_URI;
	private URI updateUri = DEFAULT_UPDATE_URI;
	private URI deactivateUri = DEFAULT_DEACTIVATE_URI;
	private URI executeUri = DEFAULT_EXECUTE_URI;
	private URI createResourceUri = DEFAULT_CREATE_RESOURCE_URI;
	private URI updateResourceUri = DEFAULT_UPDATE_RESOURCE_URI;
	private URI deactivateResourceUri = DEFAULT_DEACTIVATE_RESOURCE_URI;
	private URI propertiesUri = DEFAULT_PROPERTIES_URI;
	private URI methodsUri = DEFAULT_METHODS_URI;
	private URI traitsUri = DEFAULT_TRAITS_URI;

	public ClientUniRegistrar() {

	}

	public static ClientUniRegistrar create(URI baseUri) {

		if (! baseUri.toString().endsWith("/")) baseUri = URI.create(baseUri + "/");

		ClientUniRegistrar clientUniRegistrar = new ClientUniRegistrar();
		clientUniRegistrar.setCreateUri(URI.create(baseUri + "create"));
		clientUniRegistrar.setUpdateUri(URI.create(baseUri + "update"));
		clientUniRegistrar.setDeactivateUri(URI.create(baseUri + "deactivate"));
		clientUniRegistrar.setExecuteUri(URI.create(baseUri + "execute"));
		clientUniRegistrar.setCreateResourceUri(URI.create(baseUri + "createResource"));
		clientUniRegistrar.setUpdateResourceUri(URI.create(baseUri + "updateResource"));
		clientUniRegistrar.setDeactivateResourceUri(URI.create(baseUri + "deactivateResource"));
		clientUniRegistrar.setPropertiesUri(URI.create(baseUri + "properties"));
		clientUniRegistrar.setMethodsUri(URI.create(baseUri + "methods"));

		return clientUniRegistrar;
	}

	@Override
	public CreateState create(String method, CreateRequest createRequest) throws RegistrationException {

		if (method == null) throw new NullPointerException();
		if (createRequest == null) throw new NullPointerException();

		// prepare HTTP request

		String uriString = this.getCreateUri().toString() + "?method=" + method;

		String body = HttpBindingUtil.toHttpBodyRequest(createRequest);

		HttpPost httpPost = new HttpPost(URI.create(uriString));
		httpPost.setEntity(new StringEntity(body, ContentType.create(RegistrationMediaTypes.REQUEST_MEDIA_TYPE, StandardCharsets.UTF_8)));
		httpPost.addHeader("Accept", RegistrationMediaTypes.STATE_MEDIA_TYPE);
		if (this.getHttpHeaders() != null) this.getHttpHeaders().forEach(httpPost::addHeader);

		// execute HTTP request

		CreateState createState;

		if (log.isDebugEnabled()) log.debug("Request for CREATE REQUEST " + body + " to: " + uriString);

		try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpPost)) {

			int statusCode = httpResponse.getStatusLine().getStatusCode();
			String statusMessage = httpResponse.getStatusLine().getReasonPhrase();

			if (log.isDebugEnabled()) log.debug("Response status from " + uriString + ": " + statusCode + " " + statusMessage);

			if (statusCode == 404) return null;

			HttpEntity httpEntity = httpResponse.getEntity();
			String httpBody = EntityUtils.toString(httpEntity);
			EntityUtils.consume(httpEntity);

			if (log.isDebugEnabled()) log.debug("Response body from " + uriString + ": " + httpBody);

			if (httpResponse.getStatusLine().getStatusCode() >= 300) {

				if (log.isWarnEnabled()) log.warn("Cannot retrieve CREATE STATE for CREATE REQUEST " + createRequest + " from " + uriString + ": " + httpBody);
				throw new RegistrationException(httpBody);
			}

			createState = HttpBindingUtil.fromHttpBodyState(httpBody, CreateState.class);
		} catch (IOException ex) {

			throw new RegistrationException("Cannot retrieve CREATE STATE for CREATE REQUEST " + createRequest + " from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved CREATE STATE for CREATE REQUEST " + createRequest + " (" + uriString + "): " + createState);

		// done

		return createState;
	}

	@Override
	public UpdateState update(String method, UpdateRequest updateRequest) throws RegistrationException {

		if (method == null) throw new NullPointerException();
		if (updateRequest == null) throw new NullPointerException();

		// prepare HTTP request

		String uriString = this.getUpdateUri().toString() + "?method=" + method;

		String body = HttpBindingUtil.toHttpBodyRequest(updateRequest);

		HttpPost httpPost = new HttpPost(URI.create(uriString));
		httpPost.setEntity(new StringEntity(body, ContentType.create(RegistrationMediaTypes.REQUEST_MEDIA_TYPE, StandardCharsets.UTF_8)));
		httpPost.addHeader("Accept", RegistrationMediaTypes.STATE_MEDIA_TYPE);
		if (this.getHttpHeaders() != null) this.getHttpHeaders().forEach(httpPost::addHeader);

		// execute HTTP request

		UpdateState updateState;

		if (log.isDebugEnabled()) log.debug("Request for UPDATE REQUEST " + body + " to: " + uriString);

		try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpPost)) {

			int statusCode = httpResponse.getStatusLine().getStatusCode();
			String statusMessage = httpResponse.getStatusLine().getReasonPhrase();

			if (log.isDebugEnabled()) log.debug("Response status from " + uriString + ": " + statusCode + " " + statusMessage);

			if (statusCode == 404) return null;

			HttpEntity httpEntity = httpResponse.getEntity();
			String httpBody = EntityUtils.toString(httpEntity);
			EntityUtils.consume(httpEntity);

			if (log.isDebugEnabled()) log.debug("Response body from " + uriString + ": " + httpBody);

			if (httpResponse.getStatusLine().getStatusCode() >= 300) {

				if (log.isWarnEnabled()) log.warn("Cannot retrieve UPDATE STATE for UPDATE REQUEST " + updateRequest + " from " + uriString + ": " + httpBody);
				throw new RegistrationException(httpBody);
			}

			updateState = HttpBindingUtil.fromHttpBodyState(httpBody, UpdateState.class);
		} catch (IOException ex) {

			throw new RegistrationException("Cannot retrieve UPDATE STATE for UPDATE REQUEST " + updateRequest + " from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved UPDATE STATE for UPDATE REQUEST " + updateRequest + " (" + uriString + "): " + updateState);

		// done

		return updateState;
	}

	@Override
	public DeactivateState deactivate(String method, DeactivateRequest deactivateRequest) throws RegistrationException {

		if (method == null) throw new NullPointerException();
		if (deactivateRequest == null) throw new NullPointerException();

		// prepare HTTP request

		String uriString = this.getDeactivateUri().toString() + "?method=" + method;

		String body = HttpBindingUtil.toHttpBodyRequest(deactivateRequest);

		HttpPost httpPost = new HttpPost(URI.create(uriString));
		httpPost.setEntity(new StringEntity(body, ContentType.create(RegistrationMediaTypes.REQUEST_MEDIA_TYPE, StandardCharsets.UTF_8)));
		httpPost.addHeader("Accept", RegistrationMediaTypes.STATE_MEDIA_TYPE);
		if (this.getHttpHeaders() != null) this.getHttpHeaders().forEach(httpPost::addHeader);

		// execute HTTP request

		DeactivateState deactivateState;

		if (log.isDebugEnabled()) log.debug("Request for DEACTIVATE REQUEST " + body + " to: " + uriString);

		try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpPost)) {

			int statusCode = httpResponse.getStatusLine().getStatusCode();
			String statusMessage = httpResponse.getStatusLine().getReasonPhrase();

			if (log.isDebugEnabled()) log.debug("Response status from " + uriString + ": " + statusCode + " " + statusMessage);

			if (statusCode == 404) return null;

			HttpEntity httpEntity = httpResponse.getEntity();
			String httpBody = EntityUtils.toString(httpEntity);
			EntityUtils.consume(httpEntity);

			if (log.isDebugEnabled()) log.debug("Response body from " + uriString + ": " + httpBody);

			if (httpResponse.getStatusLine().getStatusCode() >= 300) {

				if (log.isWarnEnabled()) log.warn("Cannot retrieve DEACTIVATE STATE for DEACTIVATE REQUEST " + deactivateRequest + " from " + uriString + ": " + httpBody);
				throw new RegistrationException(httpBody);
			}

			deactivateState = HttpBindingUtil.fromHttpBodyState(httpBody, DeactivateState.class);
		} catch (IOException ex) {

			throw new RegistrationException("Cannot retrieve DEACTIVATE STATE for DEACTIVATE REQUEST " + deactivateRequest + " from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved DEACTIVATE STATE for DEACTIVATE REQUEST " + deactivateRequest + " (" + uriString + "): " + deactivateState);

		// done

		return deactivateState;
	}

	@Override
	public ExecuteState execute(String method, ExecuteRequest executeRequest) throws RegistrationException {

		if (method == null) throw new NullPointerException();
		if (executeRequest == null) throw new NullPointerException();

		// prepare HTTP request

		String uriString = this.getExecuteUri().toString() + "?method=" + method;

		String body = HttpBindingUtil.toHttpBodyRequest(executeRequest);

		HttpPost httpPost = new HttpPost(URI.create(uriString));
		httpPost.setEntity(new StringEntity(body, ContentType.create(RegistrationMediaTypes.REQUEST_MEDIA_TYPE, StandardCharsets.UTF_8)));
		httpPost.addHeader("Accept", RegistrationMediaTypes.STATE_MEDIA_TYPE);
		if (this.getHttpHeaders() != null) this.getHttpHeaders().forEach(httpPost::addHeader);

		// execute HTTP request

		ExecuteState executeState;

		if (log.isDebugEnabled()) log.debug("Request for EXECUTE REQUEST " + body + " to: " + uriString);

		try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpPost)) {

			int statusCode = httpResponse.getStatusLine().getStatusCode();
			String statusMessage = httpResponse.getStatusLine().getReasonPhrase();

			if (log.isDebugEnabled()) log.debug("Response status from " + uriString + ": " + statusCode + " " + statusMessage);

			if (statusCode == 404) return null;

			HttpEntity httpEntity = httpResponse.getEntity();
			String httpBody = EntityUtils.toString(httpEntity);
			EntityUtils.consume(httpEntity);

			if (log.isDebugEnabled()) log.debug("Response body from " + uriString + ": " + httpBody);

			if (httpResponse.getStatusLine().getStatusCode() >= 300) {

				if (log.isWarnEnabled()) log.warn("Cannot retrieve EXECUTE STATE for EXECUTE REQUEST " + executeRequest + " from " + uriString + ": " + httpBody);
				throw new RegistrationException(httpBody);
			}

			executeState = HttpBindingUtil.fromHttpBodyState(httpBody, ExecuteState.class);
		} catch (IOException ex) {

			throw new RegistrationException("Cannot retrieve EXECUTE STATE for EXECUTE REQUEST " + executeRequest + " from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved EXECUTE STATE for EXECUTE REQUEST " + executeRequest + " (" + uriString + "): " + executeState);

		// done

		return executeState;
	}

	@Override
	public CreateResourceState createResource(String method, CreateResourceRequest createResourceRequest) throws RegistrationException {

		if (method == null) throw new NullPointerException();
		if (createResourceRequest == null) throw new NullPointerException();

		// prepare HTTP request

		String uriString = this.getCreateResourceUri().toString() + "?method=" + method;

		String body = HttpBindingUtil.toHttpBodyRequest(createResourceRequest);

		HttpPost httpPost = new HttpPost(URI.create(uriString));
		httpPost.setEntity(new StringEntity(body, ContentType.create(RegistrationMediaTypes.REQUEST_MEDIA_TYPE, StandardCharsets.UTF_8)));
		httpPost.addHeader("Accept", RegistrationMediaTypes.STATE_MEDIA_TYPE);
		if (this.getHttpHeaders() != null) this.getHttpHeaders().forEach(httpPost::addHeader);

		// execute HTTP request

		CreateResourceState createResourceState;

		if (log.isDebugEnabled()) log.debug("Request for CREATE RESOURCE REQUEST " + body + " to: " + uriString);

		try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpPost)) {

			int statusCode = httpResponse.getStatusLine().getStatusCode();
			String statusMessage = httpResponse.getStatusLine().getReasonPhrase();

			if (log.isDebugEnabled()) log.debug("Response status from " + uriString + ": " + statusCode + " " + statusMessage);

			if (statusCode == 404) return null;

			HttpEntity httpEntity = httpResponse.getEntity();
			String httpBody = EntityUtils.toString(httpEntity);
			EntityUtils.consume(httpEntity);

			if (log.isDebugEnabled()) log.debug("Response body from " + uriString + ": " + httpBody);

			if (httpResponse.getStatusLine().getStatusCode() >= 300) {

				if (log.isWarnEnabled()) log.warn("Cannot retrieve CREATE RESOURCE STATE for CREATE RESOURCE REQUEST " + createResourceRequest + " from " + uriString + ": " + httpBody);
				throw new RegistrationException(httpBody);
			}

			createResourceState = HttpBindingUtil.fromHttpBodyResourceState(httpBody, CreateResourceState.class);
		} catch (IOException ex) {

			throw new RegistrationException("Cannot retrieve CREATE RESOURCE STATE for CREATE RESOURCE REQUEST " + createResourceRequest + " from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved CREATE RESOURCE STATE for CREATE RESOURCE REQUEST " + createResourceRequest + " (" + uriString + "): " + createResourceState);

		// done

		return createResourceState;
	}

	@Override
	public UpdateResourceState updateResource(String method, UpdateResourceRequest updateResourceRequest) throws RegistrationException {

		if (method == null) throw new NullPointerException();
		if (updateResourceRequest == null) throw new NullPointerException();

		// prepare HTTP request

		String uriString = this.getUpdateResourceUri().toString() + "?method=" + method;

		String body = HttpBindingUtil.toHttpBodyRequest(updateResourceRequest);

		HttpPost httpPost = new HttpPost(URI.create(uriString));
		httpPost.setEntity(new StringEntity(body, ContentType.create(RegistrationMediaTypes.REQUEST_MEDIA_TYPE, StandardCharsets.UTF_8)));
		httpPost.addHeader("Accept", RegistrationMediaTypes.STATE_MEDIA_TYPE);
		if (this.getHttpHeaders() != null) this.getHttpHeaders().forEach(httpPost::addHeader);

		// execute HTTP request

		UpdateResourceState updateResourceState;

		if (log.isDebugEnabled()) log.debug("Request for UPDATE RESOURCE REQUEST " + body + " to: " + uriString);

		try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpPost)) {

			int statusCode = httpResponse.getStatusLine().getStatusCode();
			String statusMessage = httpResponse.getStatusLine().getReasonPhrase();

			if (log.isDebugEnabled()) log.debug("Response status from " + uriString + ": " + statusCode + " " + statusMessage);

			if (statusCode == 404) return null;

			HttpEntity httpEntity = httpResponse.getEntity();
			String httpBody = EntityUtils.toString(httpEntity);
			EntityUtils.consume(httpEntity);

			if (log.isDebugEnabled()) log.debug("Response body from " + uriString + ": " + httpBody);

			if (httpResponse.getStatusLine().getStatusCode() >= 300) {

				if (log.isWarnEnabled()) log.warn("Cannot retrieve UPDATE RESOURCE STATE for UPDATE RESOURCE REQUEST " + updateResourceRequest + " from " + uriString + ": " + httpBody);
				throw new RegistrationException(httpBody);
			}

			updateResourceState = HttpBindingUtil.fromHttpBodyResourceState(httpBody, UpdateResourceState.class);
		} catch (IOException ex) {

			throw new RegistrationException("Cannot retrieve UPDATE RESOURCE STATE for UPDATE RESOURCE REQUEST " + updateResourceRequest + " from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved UPDATE RESOURCE STATE for UPDATE RESOURCE REQUEST " + updateResourceRequest + " (" + uriString + "): " + updateResourceState);

		// done

		return updateResourceState;
	}

	@Override
	public DeactivateResourceState deactivateResource(String method, DeactivateResourceRequest deactivateResourceRequest) throws RegistrationException {

		if (method == null) throw new NullPointerException();
		if (deactivateResourceRequest == null) throw new NullPointerException();

		// prepare HTTP request

		String uriString = this.getDeactivateResourceUri().toString() + "?method=" + method;

		String body = HttpBindingUtil.toHttpBodyRequest(deactivateResourceRequest);

		HttpPost httpPost = new HttpPost(URI.create(uriString));
		httpPost.setEntity(new StringEntity(body, ContentType.create(RegistrationMediaTypes.REQUEST_MEDIA_TYPE, StandardCharsets.UTF_8)));
		httpPost.addHeader("Accept", RegistrationMediaTypes.STATE_MEDIA_TYPE);
		if (this.getHttpHeaders() != null) this.getHttpHeaders().forEach(httpPost::addHeader);

		// execute HTTP request

		DeactivateResourceState deactivateResourceState;

		if (log.isDebugEnabled()) log.debug("Request for DEACTIVATE RESOURCE REQUEST " + body + " to: " + uriString);

		try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.getHttpClient().execute(httpPost)) {

			int statusCode = httpResponse.getStatusLine().getStatusCode();
			String statusMessage = httpResponse.getStatusLine().getReasonPhrase();

			if (log.isDebugEnabled()) log.debug("Response status from " + uriString + ": " + statusCode + " " + statusMessage);

			if (statusCode == 404) return null;

			HttpEntity httpEntity = httpResponse.getEntity();
			String httpBody = EntityUtils.toString(httpEntity);
			EntityUtils.consume(httpEntity);

			if (log.isDebugEnabled()) log.debug("Response body from " + uriString + ": " + httpBody);

			if (httpResponse.getStatusLine().getStatusCode() >= 300) {

				if (log.isWarnEnabled()) log.warn("Cannot retrieve DEACTIVATE RESOURCE STATE for DEACTIVATE RESOURCE REQUEST " + deactivateResourceRequest + " from " + uriString + ": " + httpBody);
				throw new RegistrationException(httpBody);
			}

			deactivateResourceState = HttpBindingUtil.fromHttpBodyResourceState(httpBody, DeactivateResourceState.class);
		} catch (IOException ex) {

			throw new RegistrationException("Cannot retrieve DEACTIVATE RESOURCE STATE for DEACTIVATE RESOURCE REQUEST " + deactivateResourceRequest + " from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved DEACTIVATE RESOURCE STATE for DEACTIVATE RESOURCE REQUEST " + deactivateResourceRequest + " (" + uriString + "): " + deactivateResourceState);

		// done

		return deactivateResourceState;
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

			if (httpResponse.getStatusLine().getStatusCode() >= 300) {

				if (log.isWarnEnabled()) log.warn("Cannot retrieve PROPERTIES from " + uriString + ": " + httpBody);
				throw new RegistrationException(httpBody);
			}

			properties = (Map<String, Map<String, Object>>) objectMapper.readValue(httpBody, LinkedHashMap.class);
		} catch (IOException ex) {

			throw new RegistrationException("Cannot retrieve PROPERTIES from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved PROPERTIES (" + uriString + "): " + properties);

		// done

		return properties;
	}

	@Override
	public Set<String> methods() throws RegistrationException {

		// prepare HTTP request

		String uriString = this.getMethodsUri().toString();

		HttpGet httpGet = new HttpGet(URI.create(uriString));
		httpGet.addHeader("Accept", UniRegistrar.METHODS_MIME_TYPE);

		// execute HTTP request

		Set<String> methods;

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

			if (httpResponse.getStatusLine().getStatusCode() >= 300) {

				if (log.isWarnEnabled()) log.warn("Cannot retrieve METHODS from " + uriString + ": " + httpBody);
				throw new RegistrationException(httpBody);
			}

			methods = (Set<String>) objectMapper.readValue(httpBody, LinkedHashSet.class);
		} catch (IOException ex) {

			throw new RegistrationException("Cannot retrieve METHODS from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved METHODS (" + uriString + "): " + methods);

		// done

		return methods;
	}

	@Override
	public Map<String, Map<String, Object>> traits() throws RegistrationException {

		// prepare HTTP request

		String uriString = this.getTraitsUri().toString();

		HttpGet httpGet = new HttpGet(URI.create(uriString));
		httpGet.addHeader("Accept", UniRegistrar.TRAITS_MIME_TYPE);

		// execute HTTP request

		Map<String, Map<String, Object>> traits;

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

				if (log.isWarnEnabled()) log.warn("Cannot retrieve TRAITS from " + uriString + ": " + httpBody);
				throw new RegistrationException(httpBody);
			}

			traits = (Map<String, Map<String, Object>>) objectMapper.readValue(httpBody, LinkedHashMap.class);
		} catch (IOException ex) {

			throw new RegistrationException("Cannot retrieve TRAITS from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved TRAITS (" + uriString + "): " + traits);

		// done

		return traits;
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

	public Map<String, String> getHttpHeaders() {
		return this.httpHeaders;
	}

	public void setHttpHeaders(Map<String, String> httpHeaders) {
		this.httpHeaders = httpHeaders;
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

	public URI getExecuteUri() {
		return this.executeUri;
	}

	public void setExecuteUri(URI executeUri) {
		this.executeUri = executeUri;
	}

	public void setExecuteUri(String executeUri) {
		this.executeUri = URI.create(executeUri);
	}

	public URI getCreateResourceUri() {
		return createResourceUri;
	}

	public void setCreateResourceUri(URI createResourceUri) {
		this.createResourceUri = createResourceUri;
	}

	public URI getUpdateResourceUri() {
		return updateResourceUri;
	}

	public void setUpdateResourceUri(URI updateResourceUri) {
		this.updateResourceUri = updateResourceUri;
	}

	public URI getDeactivateResourceUri() {
		return deactivateResourceUri;
	}

	public void setDeactivateResourceUri(URI deactivateResourceUri) {
		this.deactivateResourceUri = deactivateResourceUri;
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

	public URI getMethodsUri() {
		return this.methodsUri;
	}

	public void setMethodsUri(URI methodsUri) {
		this.methodsUri = methodsUri;
	}

	public URI getTraitsUri() {
		return traitsUri;
	}

	public void setTraitsUri(URI traitsUri) {
		this.traitsUri = traitsUri;
	}
}
