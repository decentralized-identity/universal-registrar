package uniregistrar.web.servlet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import foundation.identity.did.DID;
import foundation.identity.did.parser.ParserException;
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
import uniregistrar.openapi.model.DeactivateRequest;
import uniregistrar.openapi.model.RegistrarState;
import uniregistrar.util.HttpBindingUtil;
import uniregistrar.web.WebUniRegistrar;

import java.io.IOException;
import java.util.Map;

public class DeactivateServlet extends WebUniRegistrar {

	protected static final Logger log = LoggerFactory.getLogger(DeactivateServlet.class);

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// read request

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		Map<String, Object> requestMap;

		try {
			requestMap = HttpBindingUtil.fromHttpBodyMap(request.getReader());
		} catch (Exception ex) {
			if (log.isWarnEnabled()) log.warn("Cannot parse DEACTIVATE request (JSON): " + ex.getMessage(), ex);
			ServletUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Cannot parse DEACTIVATE request (JSON): " + ex.getMessage());
			return;
		}

		final String method;
		if (request.getParameter("method") != null) {
			method = request.getParameter("method");
		} else {
			Object didString = requestMap.get("did");
			if (didString instanceof String) {
				if (log.isInfoEnabled()) log.info("Found DID in DEACTIVATE request: " + didString);
				try {
					DID did = DID.fromString((String) didString);
					method = did.getMethodName();
				} catch (ParserException ex) {
					ServletUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Cannot parse DID: " + didString);
					return;
				}
			} else {
				method = null;
			}
		}
		if (method == null) {
			if (log.isWarnEnabled()) log.warn("Missing DID method in DEACTIVATE request.");
			ServletUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Missing DID method in DEACTIVATE request.");
			return;
		}

		if (log.isInfoEnabled()) log.info("Incoming DEACTIVATE request for method " + method + ": " + requestMap);

		// [before read]

		if (this.getUniRegistrar() instanceof LocalUniRegistrar) {
			try {
				LocalUniRegistrar localUniRegistrar = ((LocalUniRegistrar) this.getUniRegistrar());
				localUniRegistrar.executeExtensions(Extension.BeforeReadDeactivateExtension.class, e -> e.beforeReadDeactivate(method, requestMap, localUniRegistrar), requestMap);
			} catch (Exception ex) {
				if (log.isWarnEnabled()) log.warn("Cannot parse DEACTIVATE request (extension): " + ex.getMessage(), ex);
				ServletUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Cannot parse DEACTIVATE request (extension): " + ex.getMessage());
				return;
			}
		}

		// parse request

		DeactivateRequest deactivateRequest;

		try {
			deactivateRequest = HttpBindingUtil.fromMapRequest(requestMap, DeactivateRequest.class);
		} catch (Exception ex) {
			if (log.isWarnEnabled()) log.warn("Cannot parse DEACTIVATE request (object): " + ex.getMessage(), ex);
			ServletUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Cannot parse DEACTIVATE request (object): " + ex.getMessage());
			return;
		}

		if (log.isInfoEnabled()) log.info("Parsed DEACTIVATE request for method " + method + ": " + deactivateRequest);

		if (deactivateRequest == null) {

			ServletUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "No valid DEACTIVATE request found.");
			return;
		}

		// execute the request

		RegistrarState state = null;
		final Map<String, Object> stateMap;

		try {

			state = this.deactivate(method, deactivateRequest);
			if (state == null) throw new RegistrationException("No state.");
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("DEACTIVATE problem for " + deactivateRequest + ": " + ex.getMessage(), ex);

			if (! (ex instanceof RegistrationException)) ex = new RegistrationException("DEACTIVATE problem for " + deactivateRequest + ": " + ex.getMessage());
			state = ((RegistrationException) ex).toFailedState();
		} finally {
			stateMap = state == null ? null : HttpBindingUtil.toMapState(state);
		}

		if (log.isInfoEnabled()) log.info("DEACTIVATE state for " + deactivateRequest + ": " + state);

		// [before write]

		if (this.getUniRegistrar() instanceof LocalUniRegistrar) {
			try {
				LocalUniRegistrar localUniRegistrar = ((LocalUniRegistrar) this.getUniRegistrar());
				localUniRegistrar.executeExtensions(Extension.BeforeWriteDeactivateExtension.class, e -> e.beforeWriteDeactivate(method, stateMap, localUniRegistrar), stateMap);
			} catch (Exception ex) {
				if (log.isWarnEnabled()) log.warn("Cannot write DEACTIVATE state (extension): " + ex.getMessage(), ex);
				ServletUtil.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot write DEACTIVATE state (extension): " + ex.getMessage());
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