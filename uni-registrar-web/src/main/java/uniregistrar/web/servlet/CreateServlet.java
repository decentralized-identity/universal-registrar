package uniregistrar.web.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniregistrar.RegistrationException;
import uniregistrar.driver.util.HttpBindingServerUtil;
import uniregistrar.local.LocalUniRegistrar;
import uniregistrar.local.extensions.Extension;
import uniregistrar.request.CreateRequest;
import uniregistrar.state.State;
import uniregistrar.web.WebUniRegistrar;

import java.io.IOException;
import java.util.Map;

public class CreateServlet extends WebUniRegistrar {

	protected static Logger log = LoggerFactory.getLogger(CreateServlet.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();

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
			createRequest = CreateRequest.fromMap(requestMap);
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

		State state = null;
		final Map<String, Object> stateMap;

		try {

			state = this.create(method, createRequest);
			if (state == null) throw new RegistrationException("No state.");
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("CREATE problem for " + createRequest + ": " + ex.getMessage(), ex);

			if (! (ex instanceof RegistrationException)) ex = new RegistrationException("CREATE problem for " + createRequest + ": " + ex.getMessage());
			state = ((RegistrationException) ex).toFailedState();
		} finally {
			stateMap = state == null ? null : state.toMap();
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
				State.MEDIA_TYPE,
				HttpBindingServerUtil.toHttpBodyStreamState(stateMap));
	}
}