package uniregistrar.web.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniregistrar.RegistrationException;
import uniregistrar.driver.util.HttpBindingServerUtil;
import uniregistrar.request.DeactivateRequest;
import uniregistrar.state.State;
import uniregistrar.web.WebUniRegistrar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
			ServletUtil.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, "Request problem: " + ex.getMessage());
			return;
		}

		String method = request.getParameter("method");

		if (log.isInfoEnabled()) log.info("Incoming deactivate request for method " + method + ": " + deactivateRequest);

		if (deactivateRequest == null) {

			ServletUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, null, "No deactivate request found.");
			return;
		}

		// execute the request

		State state;

		try {

			state = this.deactivate(method, deactivateRequest);
			if (state == null) throw new RegistrationException("No state.");
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Deactivate problem for " + deactivateRequest + ": " + ex.getMessage(), ex);

			if (! (ex instanceof RegistrationException)) ex = new RegistrationException("Deactivate problem for " + deactivateRequest + ": " + ex.getMessage());
			state = ((RegistrationException) ex).toFailedState();
		}

		if (log.isInfoEnabled()) log.info("State for " + deactivateRequest + ": " + state);

		// write state

		ServletUtil.sendResponse(
				response,
				HttpBindingServerUtil.httpStatusCodeForState(state),
				State.MEDIA_TYPE,
				HttpBindingServerUtil.toHttpBodyStreamState(state));
	}
}