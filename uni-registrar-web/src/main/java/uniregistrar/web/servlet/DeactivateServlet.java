package uniregistrar.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uniregistrar.request.DeactivateRequest;
import uniregistrar.state.DeactivateState;
import uniregistrar.web.WebUniRegistrar;

public class DeactivateServlet extends WebUniRegistrar {

	protected static Logger log = LoggerFactory.getLogger(DeactivateServlet.class);

	public static final String MIME_TYPE = "application/json";

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// read request

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		DeactivateRequest deactivateRequest;

		try {

			deactivateRequest = DeactivateRequest.fromJson(request.getReader());
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Request problem: " + ex.getMessage(), ex);
			WebUniRegistrar.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, "Request problem: " + ex.getMessage());
			return;
		}

		String method = request.getParameter("method");

		if (log.isInfoEnabled()) log.info("Incoming deactivate request for method " + method + ": " + deactivateRequest);

		if (deactivateRequest == null) {

			WebUniRegistrar.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, null, "No deactivate request found.");
			return;
		}

		// execute the request

		DeactivateState deactiateState;
		String deactivateStateString;

		try {

			deactiateState = this.deactivate(method, deactivateRequest);
			deactivateStateString = deactiateState == null ? null : deactiateState.toJson();
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Deactivate problem for " + deactivateRequest + ": " + ex.getMessage(), ex);
			WebUniRegistrar.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, "Deactivate problem for " + deactivateRequest + ": " + ex.getMessage());
			return;
		}

		if (log.isInfoEnabled()) log.info("Deactivate state for " + deactivateRequest + ": " + deactivateStateString);

		// no deactivate state?

		if (deactivateStateString == null) {

			WebUniRegistrar.sendResponse(response, HttpServletResponse.SC_NOT_FOUND, null, "No deactivate state for " + deactivateRequest + ".");
			return;
		}

		// write deactivate state

		WebUniRegistrar.sendResponse(response, HttpServletResponse.SC_OK, MIME_TYPE, deactivateStateString);
	}
}