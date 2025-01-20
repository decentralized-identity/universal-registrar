package uniregistrar.driver.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class HttpDriver implements Driver {

	private static final Logger log = LoggerFactory.getLogger(HttpDriver.class);

	private static final int HTTP_CLIENT_TIMEOUT = 60;

	private HttpClient httpClient;
	private String method;
	private URI createUri = null;
	private URI updateUri = null;
	private URI deactivateUri = null;
	private URI executeUri = null;
	private URI createResourceUri = null;
	private URI updateResourceUri = null;
	private URI deactivateResourceUri = null;
	private URI propertiesUri = null;
	private Boolean includeMethodParameter = false;
	private Map<String, Object> traits = Collections.emptyMap();

	private Consumer<Map<String, Object>> beforeWriteCreateConsumer;
	private Consumer<Map<String, Object>> beforeReadCreateConsumer;
	private Consumer<Map<String, Object>> beforeWriteUpdateConsumer;
	private Consumer<Map<String, Object>> beforeReadUpdateConsumer;
	private Consumer<Map<String, Object>> beforeWriteDeactivateConsumer;
	private Consumer<Map<String, Object>> beforeReadDeactivateConsumer;
	private Consumer<Map<String, Object>> beforeWriteExecuteConsumer;
	private Consumer<Map<String, Object>> beforeReadExecuteConsumer;
	private Consumer<Map<String, Object>> beforeWriteCreateResourceConsumer;
	private Consumer<Map<String, Object>> beforeReadCreateResourceConsumer;
	private Consumer<Map<String, Object>> beforeWriteUpdateResourceConsumer;
	private Consumer<Map<String, Object>> beforeReadUpdateResourceConsumer;
	private Consumer<Map<String, Object>> beforeWriteDeactivateResourceConsumer;
	private Consumer<Map<String, Object>> beforeReadDeactivateResourceConsumer;
	private Consumer<Map<String, Object>> beforeWriteExecuteResourceConsumer;
	private Consumer<Map<String, Object>> beforeReadExecuteResourceConsumer;

	public HttpDriver() {
		this.httpClient = buildDefaultHttpClient();
	}

	private static HttpClient buildDefaultHttpClient() {
		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectTimeout(HTTP_CLIENT_TIMEOUT * 1000)
				.setConnectionRequestTimeout(HTTP_CLIENT_TIMEOUT * 1000)
				.setSocketTimeout(HTTP_CLIENT_TIMEOUT * 1000)
				.build();
		return HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
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
				throw RegistrationException.fromRegistrarState(createState);
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
				throw RegistrationException.fromRegistrarState(updateState);
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
				throw RegistrationException.fromRegistrarState(deactivateState);
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
	public ExecuteState execute(ExecuteRequest executeRequest) throws RegistrationException {

		// prepare HTTP request

		String uriString = this.getExecuteUri().toString();

		Map<String, Object> requestMap = HttpBindingUtil.toMapRequest(executeRequest);
		this.getBeforeWriteExecuteConsumer().accept(requestMap);

		String httpRequestBodyString = HttpBindingUtil.toHttpBodyMap(requestMap);

		HttpPost httpPost = new HttpPost(URI.create(uriString));
		httpPost.setEntity(new StringEntity(httpRequestBodyString, ContentType.create(RegistrationMediaTypes.REQUEST_MEDIA_TYPE, StandardCharsets.UTF_8)));
		httpPost.addHeader("Accept", RegistrationMediaTypes.STATE_MEDIA_TYPE);

		// execute HTTP request and read response

		ExecuteState executeState = null;

		if (log.isDebugEnabled()) log.debug("Driver request for EXECUTE REQUEST " + httpRequestBodyString + " to: " + uriString);

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
				this.getBeforeReadExecuteConsumer().accept(stateMap);
			} catch (JsonProcessingException ex) {
				throw new RegistrationException(RegistrationException.ERROR_INTERNALERROR, "Driver cannot retrieve state: " + httpResponseStatusCode + " " + httpResponseStatusMessage + " (" + httpResponseBodyString + ")");
			}

			if (isStateHttpContent(stateMap)) {
				executeState = HttpBindingUtil.fromMapState(stateMap, ExecuteState.class);
			}

			if (httpResponse.getStatusLine().getStatusCode() >= 300 && executeState == null) {
				throw new RegistrationException(RegistrationException.ERROR_INTERNALERROR, "Driver cannot retrieve error state: " + httpResponseStatusCode + " " + httpResponseStatusMessage + " (" + httpResponseBodyString + ")");
			}

			if (executeState != null && executeState.getDidState() instanceof DidStateFailed didStateFailed) {
				if (log.isWarnEnabled()) log.warn(didStateFailed.getError() + " -> " + didStateFailed.getReason());
				throw RegistrationException.fromRegistrarState(executeState);
			}

			if (executeState == null) {
				executeState = HttpBindingUtil.fromMapState(stateMap, ExecuteState.class);
			}
		} catch (IOException ex) {

			throw new RegistrationException("Cannot retrieve EXECUTE STATE for execute request " + executeRequest + " from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved EXECUTE STATE for execute request " + executeRequest + " (" + uriString + "): " + executeState);

		// done

		return executeState;
	}

	@Override
	public CreateResourceState createResource(CreateResourceRequest createResourceRequest) throws RegistrationException {

		// prepare HTTP request

		String uriString = this.getCreateResourceUri().toString();
		if (Boolean.TRUE == this.getIncludeMethodParameter()) uriString += "?method=" + this.getMethod();

		Map<String, Object> requestMap = HttpBindingUtil.toMapRequest(createResourceRequest);
		this.getBeforeWriteCreateResourceConsumer().accept(requestMap);

		String httpRequestBodyString = HttpBindingUtil.toHttpBodyMap(requestMap);

		HttpPost httpPost = new HttpPost(URI.create(uriString));
		httpPost.setEntity(new StringEntity(httpRequestBodyString, ContentType.create(RegistrationMediaTypes.REQUEST_MEDIA_TYPE, StandardCharsets.UTF_8)));
		httpPost.addHeader("Accept", RegistrationMediaTypes.STATE_MEDIA_TYPE);

		// execute HTTP request and read response

		CreateResourceState createResourceState = null;

		if (log.isDebugEnabled()) log.debug("Driver request for CREATE RESOURCE REQUEST " + httpRequestBodyString + " to: " + uriString);

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
				this.getBeforeReadCreateResourceConsumer().accept(stateMap);
			} catch (JsonProcessingException ex) {
				throw new RegistrationException(RegistrationException.ERROR_INTERNALERROR, "Driver cannot retrieve state: " + httpResponseStatusCode + " " + httpResponseStatusMessage + " (" + httpResponseBodyString + ")");
			}

			if (isStateHttpContent(stateMap)) {
				createResourceState = HttpBindingUtil.fromMapResourceState(stateMap, CreateResourceState.class);
			}

			if (httpResponse.getStatusLine().getStatusCode() >= 300 && createResourceState == null) {
				throw new RegistrationException(RegistrationException.ERROR_INTERNALERROR, "Driver cannot retrieve error state: " + httpResponseStatusCode + " " + httpResponseStatusMessage + " (" + httpResponseBodyString + ")");
			}

			if (createResourceState != null && createResourceState.getDidUrlState() instanceof DidUrlStateFailed didUrlStateFailed) {
				if (log.isWarnEnabled()) log.warn(didUrlStateFailed.getError() + " -> " + didUrlStateFailed.getReason());
				throw RegistrationException.fromRegistrarResourceState(createResourceState);
			}

			if (createResourceState == null) {
				createResourceState = HttpBindingUtil.fromMapResourceState(stateMap, CreateResourceState.class);
			}
		} catch (IOException ex) {

			throw new RegistrationException("Cannot retrieve CREATE RESOURCE STATE for CREATE RESOURCE REQUEST " + createResourceRequest + " from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved CREATE RESOURCE STATE for CREATE RESOURCE REQUEST " + createResourceRequest + " (" + uriString + "): " + createResourceState);

		// done

		return createResourceState;
	}

	@Override
	public UpdateResourceState updateResource(UpdateResourceRequest updateResourceRequest) throws RegistrationException {

		// prepare HTTP request

		String uriString = this.getUpdateResourceUri().toString();

		Map<String, Object> requestMap = HttpBindingUtil.toMapRequest(updateResourceRequest);
		this.getBeforeWriteUpdateResourceConsumer().accept(requestMap);

		String httpRequestBodyString = HttpBindingUtil.toHttpBodyMap(requestMap);

		HttpPost httpPost = new HttpPost(URI.create(uriString));
		httpPost.setEntity(new StringEntity(httpRequestBodyString, ContentType.create(RegistrationMediaTypes.REQUEST_MEDIA_TYPE, StandardCharsets.UTF_8)));
		httpPost.addHeader("Accept", RegistrationMediaTypes.STATE_MEDIA_TYPE);

		// execute HTTP request and read response

		UpdateResourceState updateResourceState = null;

		if (log.isDebugEnabled()) log.debug("Driver request for UPDATE RESOURCE REQUEST " + httpRequestBodyString + " to: " + uriString);

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
				this.getBeforeReadUpdateResourceConsumer().accept(stateMap);
			} catch (JsonProcessingException ex) {
				throw new RegistrationException(RegistrationException.ERROR_INTERNALERROR, "Driver cannot retrieve state: " + httpResponseStatusCode + " " + httpResponseStatusMessage + " (" + httpResponseBodyString + ")");
			}

			if (isStateHttpContent(stateMap)) {
				updateResourceState = HttpBindingUtil.fromMapResourceState(stateMap, UpdateResourceState.class);
			}

			if (httpResponse.getStatusLine().getStatusCode() >= 300 && updateResourceState == null) {
				throw new RegistrationException(RegistrationException.ERROR_INTERNALERROR, "Driver cannot retrieve error state: " + httpResponseStatusCode + " " + httpResponseStatusMessage + " (" + httpResponseBodyString + ")");
			}

			if (updateResourceState != null && updateResourceState.getDidUrlState() instanceof DidUrlStateFailed didUrlStateFailed) {
				if (log.isWarnEnabled()) log.warn(didUrlStateFailed.getError() + " -> " + didUrlStateFailed.getReason());
				throw RegistrationException.fromRegistrarResourceState(updateResourceState);
			}

			if (updateResourceState == null) {
				updateResourceState = HttpBindingUtil.fromMapResourceState(stateMap, UpdateResourceState.class);
			}
		} catch (IOException ex) {

			throw new RegistrationException("Cannot retrieve UPDATE RESOURCE STATE for UPDATE RESOURCE REQUEST " + updateResourceRequest + " from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved UPDATE RESOURCE STATE for UPDATE RESOURCE REQUEST " + updateResourceRequest + " (" + uriString + "): " + updateResourceState);

		// done

		return updateResourceState;
	}

	@Override
	public DeactivateResourceState deactivateResource(DeactivateResourceRequest deactivateResourceRequest) throws RegistrationException {

		// prepare HTTP request

		String uriString = this.getDeactivateResourceUri().toString();

		Map<String, Object> requestMap = HttpBindingUtil.toMapRequest(deactivateResourceRequest);
		this.getBeforeWriteDeactivateResourceConsumer().accept(requestMap);

		String httpRequestBodyString = HttpBindingUtil.toHttpBodyMap(requestMap);

		HttpPost httpPost = new HttpPost(URI.create(uriString));
		httpPost.setEntity(new StringEntity(httpRequestBodyString, ContentType.create(RegistrationMediaTypes.REQUEST_MEDIA_TYPE, StandardCharsets.UTF_8)));
		httpPost.addHeader("Accept", RegistrationMediaTypes.STATE_MEDIA_TYPE);

		// execute HTTP request and read response

		DeactivateResourceState deactivateResourceState = null;

		if (log.isDebugEnabled()) log.debug("Driver request for DEACTIVATE RESOURCE REQUEST " + httpRequestBodyString + " to: " + uriString);

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
				this.getBeforeReadDeactivateResourceConsumer().accept(stateMap);
			} catch (JsonProcessingException ex) {
				throw new RegistrationException(RegistrationException.ERROR_INTERNALERROR, "Driver cannot retrieve state: " + httpResponseStatusCode + " " + httpResponseStatusMessage + " (" + httpResponseBodyString + ")");
			}

			if (isStateHttpContent(stateMap)) {
				deactivateResourceState = HttpBindingUtil.fromMapResourceState(stateMap, DeactivateResourceState.class);
			}

			if (httpResponse.getStatusLine().getStatusCode() >= 300 && deactivateResourceState == null) {
				throw new RegistrationException(RegistrationException.ERROR_INTERNALERROR, "Driver cannot retrieve error state: " + httpResponseStatusCode + " " + httpResponseStatusMessage + " (" + httpResponseBodyString + ")");
			}

			if (deactivateResourceState != null && deactivateResourceState.getDidUrlState() instanceof DidUrlStateFailed didUrlStateFailed) {
				if (log.isWarnEnabled()) log.warn(didUrlStateFailed.getError() + " -> " + didUrlStateFailed.getReason());
				throw RegistrationException.fromRegistrarResourceState(deactivateResourceState);
			}

			if (deactivateResourceState == null) {
				deactivateResourceState = HttpBindingUtil.fromMapResourceState(stateMap, DeactivateResourceState.class);
			}
		} catch (IOException ex) {

			throw new RegistrationException("Cannot retrieve DEACTIVATE RESOURCE STATE for DEACTIVATE REQUEST " + deactivateResourceRequest + " from " + uriString + ": " + ex.getMessage(), ex);
		}

		if (log.isDebugEnabled()) log.debug("Retrieved DEACTIVATE RESOURCE STATE for DEACTIVATE REQUEST " + deactivateResourceRequest + " (" + uriString + "): " + deactivateResourceState);

		// done

		return deactivateResourceState;
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
		if (this.getExecuteUri() != null) httpProperties.put("executeUri", this.getExecuteUri().toString());
		if (this.getCreateResourceUri() != null) httpProperties.put("createResourceUri", this.getCreateResourceUri().toString());
		if (this.getUpdateResourceUri() != null) httpProperties.put("updateResourceUri", this.getUpdateResourceUri().toString());
		if (this.getDeactivateResourceUri() != null) httpProperties.put("deactivateResourceUri", this.getDeactivateResourceUri().toString());
		if (this.getPropertiesUri() != null) httpProperties.put("propertiesUri", this.getPropertiesUri().toString());
		return httpProperties;
	}

	public Map<String, Object> remoteProperties() throws RegistrationException {

		if (this.getPropertiesUri() == null) return null;

		// prepare HTTP request

		String uriString = this.getPropertiesUri().toString();

		HttpGet httpGet = new HttpGet(URI.create(uriString));
		httpGet.addHeader("Accept", Driver.PROPERTIES_MEDIA_TYPE);

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

	@Override
	public Map<String, Object> traits() {
		return this.getTraits();
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

	public HttpClient getHttpClient() {
		return this.httpClient;
	}

	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
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

	public void setCreateResourceUri(String createResourceUri) {
		this.createResourceUri = URI.create(createResourceUri);
	}

	public URI getUpdateResourceUri() {
		return updateResourceUri;
	}

	public void setUpdateResourceUri(URI updateResourceUri) {
		this.updateResourceUri = updateResourceUri;
	}

	public void setUpdateResourceUri(String updateResourceUri) {
		this.updateResourceUri = URI.create(updateResourceUri);
	}

	public URI getDeactivateResourceUri() {
		return deactivateResourceUri;
	}

	public void setDeactivateResourceUri(URI deactivateResourceUri) {
		this.deactivateResourceUri = deactivateResourceUri;
	}

	public void setDeactivateResourceUri(String deactivateResourceUri) {
		this.deactivateResourceUri = URI.create(deactivateResourceUri);
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

	public Map<String, Object> getTraits() {
		return traits;
	}

	public void setTraits(Map<String, Object> traits) {
		this.traits = traits;
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

	public Consumer<Map<String, Object>> getBeforeWriteExecuteConsumer() {
		return beforeWriteExecuteConsumer;
	}

	public void setBeforeWriteExecuteConsumer(Consumer<Map<String, Object>> beforeWriteExecuteConsumer) {
		this.beforeWriteExecuteConsumer = beforeWriteExecuteConsumer;
	}

	public Consumer<Map<String, Object>> getBeforeReadExecuteConsumer() {
		return beforeReadExecuteConsumer;
	}

	public void setBeforeReadExecuteConsumer(Consumer<Map<String, Object>> beforeReadExecuteConsumer) {
		this.beforeReadExecuteConsumer = beforeReadExecuteConsumer;
	}

	public Consumer<Map<String, Object>> getBeforeWriteCreateResourceConsumer() {
		return beforeWriteCreateResourceConsumer;
	}

	public void setBeforeWriteCreateResourceConsumer(Consumer<Map<String, Object>> beforeWriteCreateResourceConsumer) {
		this.beforeWriteCreateResourceConsumer = beforeWriteCreateResourceConsumer;
	}

	public Consumer<Map<String, Object>> getBeforeReadCreateResourceConsumer() {
		return beforeReadCreateResourceConsumer;
	}

	public void setBeforeReadCreateResourceConsumer(Consumer<Map<String, Object>> beforeReadCreateResourceConsumer) {
		this.beforeReadCreateResourceConsumer = beforeReadCreateResourceConsumer;
	}

	public Consumer<Map<String, Object>> getBeforeWriteUpdateResourceConsumer() {
		return beforeWriteUpdateResourceConsumer;
	}

	public void setBeforeWriteUpdateResourceConsumer(Consumer<Map<String, Object>> beforeWriteUpdateResourceConsumer) {
		this.beforeWriteUpdateResourceConsumer = beforeWriteUpdateResourceConsumer;
	}

	public Consumer<Map<String, Object>> getBeforeReadUpdateResourceConsumer() {
		return beforeReadUpdateResourceConsumer;
	}

	public void setBeforeReadUpdateResourceConsumer(Consumer<Map<String, Object>> beforeReadUpdateResourceConsumer) {
		this.beforeReadUpdateResourceConsumer = beforeReadUpdateResourceConsumer;
	}

	public Consumer<Map<String, Object>> getBeforeWriteDeactivateResourceConsumer() {
		return beforeWriteDeactivateResourceConsumer;
	}

	public void setBeforeWriteDeactivateResourceConsumer(Consumer<Map<String, Object>> beforeWriteDeactivateResourceConsumer) {
		this.beforeWriteDeactivateResourceConsumer = beforeWriteDeactivateResourceConsumer;
	}

	public Consumer<Map<String, Object>> getBeforeReadDeactivateResourceConsumer() {
		return beforeReadDeactivateResourceConsumer;
	}

	public void setBeforeReadDeactivateResourceConsumer(Consumer<Map<String, Object>> beforeReadDeactivateResourceConsumer) {
		this.beforeReadDeactivateResourceConsumer = beforeReadDeactivateResourceConsumer;
	}

	public Consumer<Map<String, Object>> getBeforeWriteExecuteResourceConsumer() {
		return beforeWriteExecuteResourceConsumer;
	}

	public void setBeforeWriteExecuteResourceConsumer(Consumer<Map<String, Object>> beforeWriteExecuteResourceConsumer) {
		this.beforeWriteExecuteResourceConsumer = beforeWriteExecuteResourceConsumer;
	}

	public Consumer<Map<String, Object>> getBeforeReadExecuteResourceConsumer() {
		return beforeReadExecuteResourceConsumer;
	}

	public void setBeforeReadExecuteResourceConsumer(Consumer<Map<String, Object>> beforeReadExecuteResourceConsumer) {
		this.beforeReadExecuteResourceConsumer = beforeReadExecuteResourceConsumer;
	}
}
