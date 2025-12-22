package uniregistrar.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import foundation.identity.did.DID;
import foundation.identity.did.parser.ParserException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.web.HttpRequestHandler;
import uniregistrar.RegistrationException;
import uniregistrar.UniRegistrar;
import uniregistrar.openapi.model.*;
import uniregistrar.util.HttpBindingUtil;
import uniregistrar.web.servlet.ServletUtil;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@WebServlet
public abstract class WebUniRegistrar extends HttpServlet implements HttpRequestHandler, UniRegistrar {

    private static final Logger log = LoggerFactory.getLogger(WebUniRegistrar.class);

    protected static final ObjectMapper objectMapper = new ObjectMapper();

	protected static Map<String, Object> readRequestMap(String operation, HttpServletRequest request, HttpServletResponse response) throws IOException {

		final Map<String, Object> requestMap;

		try {
			requestMap = HttpBindingUtil.fromHttpBodyMap(request.getReader());
		} catch (Exception ex) {
			if (log.isWarnEnabled()) log.warn("Cannot parse UPDATE request (JSON): " + ex.getMessage(), ex);
			ServletUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Cannot parse UPDATE request (JSON): " + ex.getMessage());
			return null;
		}

		return requestMap;
	}

	protected static String readMethod(String operation, Map<String, Object> requestMap, HttpServletRequest request, HttpServletResponse response) throws IOException {

		final String method;

		if (request.getParameter("method") != null) {
			method = request.getParameter("method");
		} else {
			Object didString = requestMap.get("did");
			if (didString instanceof String) {
				if (log.isInfoEnabled()) log.info("Found DID in " + operation + " request: " + didString);
				try {
					DID did = DID.fromString((String) didString);
					method = did.getMethodName();
				} catch (ParserException ex) {
					ServletUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Cannot parse DID: " + didString);
					return null;
				}
			} else {
				method = null;
			}
		}
		if (method == null) {
			if (log.isWarnEnabled()) log.warn("Missing DID method in " + operation + " request.");
			ServletUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Missing DID method in " + operation + " request.");
			return null;
		}

		if (log.isInfoEnabled()) log.info("Incoming " + operation + " request for method " + method + ": " + requestMap);

		return method;
	}

	protected static <T extends RegistrarRequest> T parseRequest(String method, String operation, Map<String, Object> requestMap, Class<T> registrarRequestClass, HttpServletResponse response) throws IOException {

		T registrarRequest;

		try {
			registrarRequest = HttpBindingUtil.fromMapRequest(requestMap, registrarRequestClass);
		} catch (Exception ex) {
			if (log.isWarnEnabled()) log.warn("Cannot parse " + operation + " request (object): " + ex.getMessage(), ex);
			ServletUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Cannot parse " + operation + " request (object): " + ex.getMessage());
			return null;
		}

		if (log.isInfoEnabled()) log.info("Parsed " + operation + " request for method " + method + ": " + registrarRequest);

		if (registrarRequest == null) {
			ServletUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "No valid " + operation + " request found.");
			return null;
		}

		return registrarRequest;
	}

    protected static void prepareOptions(HttpServletRequest request, RegistrarRequest registrarRequest) throws JsonProcessingException {

        String httpXConfigHeader = request.getHeader("X-Config");
        if (log.isInfoEnabled()) log.info("Incoming X-Config: header string: " + httpXConfigHeader);
        Map<String, Object> httpXConfigHeaderMap = httpXConfigHeader == null ? null : (Map<String, Object>) objectMapper.readValue(httpXConfigHeader, Map.class);
        RequestOptions requestOptions = Objects.requireNonNullElseGet(registrarRequest.getOptions(), () -> registrarRequest.options(new RequestOptions()).getOptions());
        if (httpXConfigHeaderMap != null) httpXConfigHeaderMap.forEach(requestOptions::putAdditionalProperty);

        if (log.isDebugEnabled()) log.debug("Using options: " + requestOptions);
    }

	@Autowired
	@Qualifier("UniRegistrar")
	private UniRegistrar uniRegistrar;

	public WebUniRegistrar() {

		super();
	}

	@Override
	public void handleRequest(HttpServletRequest request, @NonNull HttpServletResponse response) throws ServletException, IOException {
		if ("GET".equals(request.getMethod())) this.doGet(request, response);
		if ("POST".equals(request.getMethod())) this.doPost(request, response);
		if ("PUT".equals(request.getMethod())) this.doPut(request, response);
		if ("DELETE".equals(request.getMethod())) this.doDelete(request, response);
		if ("OPTIONS".equals(request.getMethod())) this.doOptions(request, response);
	}

	@Override
	protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Headers", "Accept, Content-Type");
		response.setStatus(HttpServletResponse.SC_OK);
	}

	@Override
	public CreateState create(String method, CreateRequest createRequest) throws RegistrationException {
		return this.getUniRegistrar() == null ? null : this.getUniRegistrar().create(method, createRequest);
	}

	@Override
	public UpdateState update(String method, UpdateRequest updateRequest) throws RegistrationException {
		return this.getUniRegistrar() == null ? null : this.getUniRegistrar().update(method, updateRequest);
	}

	@Override
	public DeactivateState deactivate(String method, DeactivateRequest deactivateRequest) throws RegistrationException {
		return this.getUniRegistrar() == null ? null : this.getUniRegistrar().deactivate(method, deactivateRequest);
	}

	@Override
	public ExecuteState execute(String method, ExecuteRequest executeRequest) throws RegistrationException {
		return this.getUniRegistrar() == null ? null : this.getUniRegistrar().execute(method, executeRequest);
	}

	@Override
	public CreateResourceState createResource(String method, CreateResourceRequest createResourceRequest) throws RegistrationException {
		return this.getUniRegistrar() == null ? null : this.getUniRegistrar().createResource(method, createResourceRequest);
	}

	@Override
	public UpdateResourceState updateResource(String method, UpdateResourceRequest updateResourceRequest) throws RegistrationException {
		return this.getUniRegistrar() == null ? null : this.getUniRegistrar().updateResource(method, updateResourceRequest);
	}

	@Override
	public DeactivateResourceState deactivateResource(String method, DeactivateResourceRequest deactivateResourceRequest) throws RegistrationException {
		return this.getUniRegistrar() == null ? null : this.getUniRegistrar().deactivateResource(method, deactivateResourceRequest);
	}

	@Override
	public Map<String, Map<String, Object>> properties() throws RegistrationException {
		return this.getUniRegistrar() == null ? null : this.getUniRegistrar().properties();
	}

	@Override
	public Set<String> methods() throws RegistrationException {
		return this.getUniRegistrar() == null ? null : this.getUniRegistrar().methods();
	}

	@Override
	public Map<String, Map<String, Object>> traits() throws RegistrationException {
		return this.getUniRegistrar() == null ? null : this.getUniRegistrar().traits();
	}

	/*
	 * Getters and setters
	 */

	public UniRegistrar getUniRegistrar() {
		return this.uniRegistrar;
	}

	public void setUniRegistrar(UniRegistrar uniRegistrar) {
		this.uniRegistrar = uniRegistrar;
	}
}
