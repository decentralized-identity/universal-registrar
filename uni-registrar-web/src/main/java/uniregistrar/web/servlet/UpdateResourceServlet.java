package uniregistrar.web.servlet;

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
import uniregistrar.openapi.model.RegistrarState;
import uniregistrar.openapi.model.UpdateRequest;
import uniregistrar.util.HttpBindingUtil;
import uniregistrar.web.WebUniRegistrar;

import java.io.IOException;
import java.util.Map;

public class UpdateResourceServlet extends WebUniRegistrar {

	protected static final Logger log = LoggerFactory.getLogger(UpdateResourceServlet.class);

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// read request

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		final Map<String, Object> requestMap;

		try {
			requestMap = HttpBindingUtil.fromHttpBodyMap(request.getReader());
		} catch (Exception ex) {
			if (log.isWarnEnabled()) log.warn("Cannot parse UPDATE request (JSON): " + ex.getMessage(), ex);
			ServletUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Cannot parse UPDATE request (JSON): " + ex.getMessage());
			return;
		}

		final String method;
		if (request.getParameter("method") != null) {
			method = request.getParameter("method");
		} else {
			Object didString = requestMap.get("did");
			if (didString instanceof String) {
				if (log.isInfoEnabled()) log.info("Found DID in UPDATE request: " + didString);
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
			if (log.isWarnEnabled()) log.warn("Missing DID method in UPDATE request.");
			ServletUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Missing DID method in UPDATE request.");
			return;
		}

		if (log.isInfoEnabled()) log.info("Incoming UPDATE request for method " + method + ": " + requestMap);

		// [before read]

		if (this.getUniRegistrar() instanceof LocalUniRegistrar) {
			try {
				LocalUniRegistrar localUniRegistrar = ((LocalUniRegistrar) this.getUniRegistrar());
				localUniRegistrar.executeExtensions(Extension.BeforeReadUpdateExtension.class, e -> e.beforeReadUpdate(method, requestMap, localUniRegistrar), requestMap);
			} catch (Exception ex) {
				if (log.isWarnEnabled()) log.warn("Cannot parse UPDATE request (extension): " + ex.getMessage(), ex);
				ServletUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Cannot parse UPDATE request (extension): " + ex.getMessage());
				return;
			}
		}

		// parse request

		UpdateRequest updateRequest;

		try {
			updateRequest = HttpBindingUtil.fromMapRequest(requestMap, UpdateRequest.class);
		} catch (Exception ex) {
			if (log.isWarnEnabled()) log.warn("Cannot parse UPDATE request (object): " + ex.getMessage(), ex);
			ServletUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Cannot parse UPDATE request (object): " + ex.getMessage());
			return;
		}

		if (log.isInfoEnabled()) log.info("Parsed UPDATE request for method " + method + ": " + updateRequest);

		if (updateRequest == null) {

			ServletUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "No valid UPDATE request found.");
			return;
		}

		// execute the request

		RegistrarState state = null;
		final Map<String, Object> stateMap;

		try {

			state = this.update(method, updateRequest);
			if (state == null) throw new RegistrationException("No state.");
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("UPDATE problem for " + updateRequest + ": " + ex.getMessage(), ex);

			if (! (ex instanceof RegistrationException)) ex = new RegistrationException("UPDATE problem for " + updateRequest + ": " + ex.getMessage());
			state = ((RegistrationException) ex).toFailedState();
		} finally {
			stateMap = state == null ? null : HttpBindingUtil.toMapState(state);
		}

		if (log.isInfoEnabled()) log.info("State for " + updateRequest + ": " + state);

		// [before write]

		if (this.getUniRegistrar() instanceof LocalUniRegistrar) {
			try {
				LocalUniRegistrar localUniRegistrar = ((LocalUniRegistrar) this.getUniRegistrar());
				localUniRegistrar.executeExtensions(Extension.BeforeWriteUpdateExtension.class, e -> e.beforeWriteUpdate(method, stateMap, localUniRegistrar), stateMap);
			} catch (Exception ex) {
				if (log.isWarnEnabled()) log.warn("Cannot write UPDATE state (extension): " + ex.getMessage(), ex);
				ServletUtil.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot write UPDATE state (extension): " + ex.getMessage());
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