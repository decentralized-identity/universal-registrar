package uniregistrar.web.servlet;

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
import uniregistrar.openapi.model.DeactivateResourceRequest;
import uniregistrar.openapi.model.RegistrarResourceState;
import uniregistrar.util.HttpBindingUtil;
import uniregistrar.web.WebUniRegistrar;

import java.io.IOException;
import java.util.Map;

public class DeactivateResourceServlet extends WebUniRegistrar {

	private static final Logger log = LoggerFactory.getLogger(DeactivateResourceServlet.class);

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// read request

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		final Map<String, Object> requestMap = readRequestMap("DEACTIVATE RESOURCE", request, response);
		if (requestMap == null) return;

		final String method = readMethod("DEACTIVATE RESOURCE", requestMap, request, response);
		if (method == null) return;

		// [before read]

		if (this.getUniRegistrar() instanceof LocalUniRegistrar) {
			try {
				LocalUniRegistrar localUniRegistrar = ((LocalUniRegistrar) this.getUniRegistrar());
				localUniRegistrar.executeExtensions(Extension.BeforeReadDeactivateResourceExtension.class, e -> e.beforeReadDeactivateResource(method, requestMap, localUniRegistrar), requestMap);
			} catch (Exception ex) {
				if (log.isWarnEnabled()) log.warn("Cannot parse DEACTIVATE RESOURCE request (extension): " + ex.getMessage(), ex);
				ServletUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Cannot parse DEACTIVATE RESOURCE request (extension): " + ex.getMessage());
				return;
			}
		}

		// parse request

		DeactivateResourceRequest deactivateResourceRequest = WebUniRegistrar.parseRequest(method, "DEACTIVATE RESOURCE", requestMap, DeactivateResourceRequest.class, response);
		if (deactivateResourceRequest == null) return;

		// prepare options

		WebUniRegistrar.prepareOptions(request, deactivateResourceRequest);

		// execute the request

		RegistrarResourceState state = null;
		final Map<String, Object> stateMap;

		try {

			state = this.deactivateResource(method, deactivateResourceRequest);
			if (state == null) throw new RegistrationException("No state.");
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("DEACTIVATE RESOURCE problem for " + deactivateResourceRequest + ": " + ex.getMessage(), ex);

			if (! (ex instanceof RegistrationException)) ex = new RegistrationException("DEACTIVATE RESOURCE problem for " + deactivateResourceRequest + ": " + ex.getMessage());
			state = ((RegistrationException) ex).toErrorRegistrarResourceState();
		} finally {
			stateMap = state == null ? null : HttpBindingUtil.toMapState(state);
		}

		if (log.isInfoEnabled()) log.info("DEACTIVATE RESOURCE state for " + deactivateResourceRequest + ": " + state);

		// [before write]

		if (this.getUniRegistrar() instanceof LocalUniRegistrar) {
			try {
				LocalUniRegistrar localUniRegistrar = ((LocalUniRegistrar) this.getUniRegistrar());
				localUniRegistrar.executeExtensions(Extension.BeforeWriteDeactivateResourceExtension.class, e -> e.beforeWriteDeactivateResource(method, stateMap, localUniRegistrar), stateMap);
			} catch (Exception ex) {
				if (log.isWarnEnabled()) log.warn("Cannot write DEACTIVATE RESOURCE state (extension): " + ex.getMessage(), ex);
				ServletUtil.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot write DEACTIVATE RESOURCE state (extension): " + ex.getMessage());
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