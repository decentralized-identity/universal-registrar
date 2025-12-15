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
import uniregistrar.openapi.model.CreateResourceRequest;
import uniregistrar.openapi.model.RegistrarResourceState;
import uniregistrar.util.HttpBindingUtil;
import uniregistrar.web.WebUniRegistrar;

import java.io.IOException;
import java.util.Map;

public class CreateResourceServlet extends WebUniRegistrar {

	private static final Logger log = LoggerFactory.getLogger(CreateResourceServlet.class);

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// read request

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		final Map<String, Object> requestMap = readRequestMap("CREATE RESOURCE", request, response);
		if (requestMap == null) return;

		final String method = readMethod("CREATE RESOURCE", requestMap, request, response);
		if (method == null) return;

		// [before read]

		if (this.getUniRegistrar() instanceof LocalUniRegistrar) {
			try {
				LocalUniRegistrar localUniRegistrar = ((LocalUniRegistrar) this.getUniRegistrar());
				localUniRegistrar.executeExtensions(Extension.BeforeReadCreateResourceExtension.class, e -> e.beforeReadCreateResource(method, requestMap, localUniRegistrar), requestMap);
			} catch (Exception ex) {
				if (log.isWarnEnabled()) log.warn("Cannot parse CREATE RESOURCE request (extension): " + ex.getMessage(), ex);
				ServletUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Cannot parse CREATE RESOURCE request (extension): " + ex.getMessage());
				return;
			}
		}

		// parse request

		CreateResourceRequest createResourceRequest = WebUniRegistrar.parseRequest(method, "CREATE RESOURCE", requestMap, CreateResourceRequest.class, response);
		if (createResourceRequest == null) return;

		// prepare options

		WebUniRegistrar.prepareOptions(request, createResourceRequest);

		// execute the request

		RegistrarResourceState state = null;
		final Map<String, Object> stateMap;

		try {

			state = this.createResource(method, createResourceRequest);
			if (state == null) throw new RegistrationException("No state.");
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("CREATE RESOURCE problem for " + createResourceRequest + ": " + ex.getMessage(), ex);

			if (! (ex instanceof RegistrationException)) ex = new RegistrationException("CREATE RESOURCE problem for " + createResourceRequest + ": " + ex.getMessage());
			state = ((RegistrationException) ex).toErrorRegistrarResourceState();
		} finally {
			stateMap = state == null ? null : HttpBindingUtil.toMapState(state);
		}

		if (log.isInfoEnabled()) log.info("CREATE RESOURCE state for " + createResourceRequest + ": " + state);

		// [before write]

		if (this.getUniRegistrar() instanceof LocalUniRegistrar) {
			try {
				LocalUniRegistrar localUniRegistrar = ((LocalUniRegistrar) this.getUniRegistrar());
				localUniRegistrar.executeExtensions(Extension.BeforeWriteCreateResourceExtension.class, e -> e.beforeWriteCreateResource(method, stateMap, localUniRegistrar), stateMap);
			} catch (Exception ex) {
				if (log.isWarnEnabled()) log.warn("Cannot write CREATE RESOURCE state (extension): " + ex.getMessage(), ex);
				ServletUtil.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot write CREATE RESOURCE state (extension): " + ex.getMessage());
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