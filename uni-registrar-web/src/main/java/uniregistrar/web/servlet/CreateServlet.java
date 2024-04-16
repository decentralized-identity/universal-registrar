package uniregistrar.web.servlet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniregistrar.RegistrationException;
import uniregistrar.RegistrationMediaTypes;
import uniregistrar.driver.util.HttpBindingServerUtil;
import uniregistrar.local.LocalUniRegistrar;
import uniregistrar.local.extensions.Extension;
import uniregistrar.openapi.RFC3339DateFormat;
import uniregistrar.openapi.model.CreateRequest;
import uniregistrar.openapi.model.RegistrarState;
import uniregistrar.web.WebUniRegistrar;

import java.io.IOException;
import java.util.Map;

public class CreateServlet extends WebUniRegistrar {

	protected static final Logger log = LoggerFactory.getLogger(CreateServlet.class);

	private static final ObjectMapper objectMapper = JsonMapper.builder()
			.serializationInclusion(JsonInclude.Include.NON_NULL)
			.disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
			.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.enable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE)
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
			.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
			.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
			.defaultDateFormat(new RFC3339DateFormat())
			.addModule(new JavaTimeModule())
			.build();

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// read request

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		final Map<String, Object> requestMap;

		try {
			requestMap = objectMapper.readValue(request.getReader(), Map.class);
		} catch (Exception ex) {
			if (log.isWarnEnabled()) log.warn("Cannot parse CREATE request (JSON): " + ex.getMessage(), ex);
			ServletUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Cannot parse CREATE request (JSON): " + ex.getMessage());
			return;
		}

		final String method = request.getParameter("method");
		if (method == null) {
			if (log.isWarnEnabled()) log.warn("Missing DID method in CREATE request.");
			ServletUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Missing DID method in CREATE request.");
			return;
		}

		if (log.isInfoEnabled()) log.info("Incoming CREATE request for method " + method + ": " + requestMap);

		// [before read]

		if (this.getUniRegistrar() instanceof LocalUniRegistrar) {
			try {
				LocalUniRegistrar localUniRegistrar = ((LocalUniRegistrar) this.getUniRegistrar());
				localUniRegistrar.executeExtensions(Extension.BeforeReadCreateExtension.class, e -> e.beforeReadCreate(method, requestMap, localUniRegistrar), requestMap);
			} catch (Exception ex) {
				if (log.isWarnEnabled()) log.warn("Cannot parse CREATE request (extension): " + ex.getMessage(), ex);
				ServletUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Cannot parse CREATE request (extension): " + ex.getMessage());
				return;
			}
		}

		// parse request

		CreateRequest createRequest;

		try {
			createRequest = objectMapper.convertValue(requestMap, CreateRequest.class);
		} catch (Exception ex) {
			if (log.isWarnEnabled()) log.warn("Cannot parse CREATE request (object): " + ex.getMessage(), ex);
			ServletUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Cannot parse CREATE request (object): " + ex.getMessage());
			return;
		}

		if (log.isInfoEnabled()) log.info("Parsed CREATE request for method " + method + ": " + createRequest);

		if (createRequest == null) {

			ServletUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "No valid CREATE request found.");
			return;
		}

		// execute the request

		RegistrarState state = null;
		final Map<String, Object> stateMap;

		try {

			state = this.create(method, createRequest);
			if (state == null) throw new RegistrationException("No state.");
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("CREATE problem for " + createRequest + ": " + ex.getMessage(), ex);

			if (! (ex instanceof RegistrationException)) ex = new RegistrationException("CREATE problem for " + createRequest + ": " + ex.getMessage());
			state = ((RegistrationException) ex).toFailedState();
		} finally {
			stateMap = state == null ? null : objectMapper.convertValue(state, Map.class);
		}

		if (log.isInfoEnabled()) log.info("CREATE state for " + createRequest + ": " + state);

		// [before write]

		if (this.getUniRegistrar() instanceof LocalUniRegistrar) {
			try {
				LocalUniRegistrar localUniRegistrar = ((LocalUniRegistrar) this.getUniRegistrar());
				localUniRegistrar.executeExtensions(Extension.BeforeWriteCreateExtension.class, e -> e.beforeWriteCreate(method, stateMap, localUniRegistrar), stateMap);
			} catch (Exception ex) {
				if (log.isWarnEnabled()) log.warn("Cannot write CREATE state (extension): " + ex.getMessage(), ex);
				ServletUtil.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot write CREATE state (extension): " + ex.getMessage());
				return;
			}
		}

		// write state

		ServletUtil.sendResponse(
				response,
				HttpBindingServerUtil.httpStatusCodeForState(state),
				RegistrationMediaTypes.STATE_MEDIA_TYPE,
				HttpBindingServerUtil.toHttpBodyStreamState(stateMap));
	}
}