package uniregistrar.driver.servlet;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uniregistrar.request.DeactivateRequest;
import uniregistrar.state.DeactivateState;

public class DeactivateServlet extends AbstractServlet implements Servlet {

	private static final long serialVersionUID = 8532462131637520098L;

	private static Logger log = LoggerFactory.getLogger(DeactivateServlet.class);

	public DeactivateServlet() {

		super();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// read request

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		DeactivateRequest deactivateRequest = DeactivateRequest.fromJson(request.getReader());

		if (log.isInfoEnabled()) log.info("Incoming deactivate request: " + deactivateRequest);

		if (deactivateRequest == null) {

			sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, null, "No deactivate request found.");
			return;
		}

		// invoke the driver

		DeactivateState deactivateState;
		String deactivateStateString;

		try {

			deactivateState = InitServlet.getDriver().deactivate(deactivateRequest);
			deactivateStateString = deactivateState == null ? null : deactivateState.toJson();
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Driver reported for " + deactivateRequest + ": " + ex.getMessage(), ex);
			sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, "Driver reported: " + ex.getMessage());
			return;
		}

		if (log.isInfoEnabled()) log.info("Deactivate state for " + deactivateRequest + ": " + deactivateStateString);

		// no deactivate state?

		if (deactivateState == null) {

			sendResponse(response, HttpServletResponse.SC_NOT_FOUND, null, "No deactivate state.");
			return;
		}

		// write deactivate state

		sendResponse(response, HttpServletResponse.SC_OK, DeactivateState.MIME_TYPE, deactivateStateString);
	}
}
