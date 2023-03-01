package uniregistrar.web.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniregistrar.RegistrationException;
import uniregistrar.driver.util.HttpBindingServerUtil;
import uniregistrar.request.UpdateRequest;
import uniregistrar.state.State;
import uniregistrar.web.WebUniRegistrar;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class UpdateServlet extends WebUniRegistrar {

	protected static Logger log = LoggerFactory.getLogger(UpdateServlet.class);

	public static final String MIME_TYPE = "application/json";

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// read request

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		UpdateRequest updateRequest;

		try {

			updateRequest = UpdateRequest.fromJson(request.getReader());
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Request problem: " + ex.getMessage(), ex);
			ServletUtil.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, "Request problem: " + ex.getMessage());
			return;
		}

		String method = request.getParameter("method");

		if (log.isInfoEnabled()) log.info("Incoming update request for method " + method + ": " + updateRequest);

		if (updateRequest == null) {

			ServletUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, null, "No update request found.");
			return;
		}

		// execute the request

		State state;

		try {

			state = this.update(method, updateRequest);
			if (state == null) throw new RegistrationException("No state.");
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Update problem for " + updateRequest + ": " + ex.getMessage(), ex);

			if (! (ex instanceof RegistrationException)) ex = new RegistrationException("Update problem for " + updateRequest + ": " + ex.getMessage());
			state = ((RegistrationException) ex).toFailedState();
		}

		if (log.isInfoEnabled()) log.info("State for " + updateRequest + ": " + state);

		// write state

		ServletUtil.sendResponse(
				response,
				HttpBindingServerUtil.httpStatusCodeForState(state),
				State.MEDIA_TYPE,
				HttpBindingServerUtil.toHttpBodyStreamState(state));
	}
}