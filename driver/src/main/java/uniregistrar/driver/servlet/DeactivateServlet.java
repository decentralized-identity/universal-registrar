package uniregistrar.driver.servlet;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniregistrar.request.DeactivateRequest;
import uniregistrar.state.DeactivateState;

import java.io.IOException;

public class DeactivateServlet extends HttpServlet implements Servlet {

	private static final Logger log = LoggerFactory.getLogger(DeactivateServlet.class);

	public DeactivateServlet() {

		super();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// read request

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		DeactivateRequest deactivateRequest = DeactivateRequest.fromJson(request.getReader());

		if (log.isInfoEnabled()) log.info("Driver: Incoming deactivate request: " + deactivateRequest);

		if (deactivateRequest == null) {

			ServletUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Driver: No deactivate request found.");
			return;
		}

		// invoke the driver

		DeactivateState deactivateState;
		String deactivateStateString;

		try {

			deactivateState = InitServlet.getDriver().deactivate(deactivateRequest);
			deactivateStateString = deactivateState == null ? null : deactivateState.toJson();
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Driver: Deactivate problem for " + deactivateRequest + ": " + ex.getMessage(), ex);
			ServletUtil.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Driver: Deactivate problem: " + ex.getMessage());
			return;
		}

		if (log.isInfoEnabled()) log.info("Driver: Deactivate state for " + deactivateRequest + ": " + deactivateStateString);

		// no deactivate state?

		if (deactivateState == null) {

			ServletUtil.sendResponse(response, HttpServletResponse.SC_NOT_FOUND, "Driver: No deactivate state.");
			return;
		}

		// write deactivate state

		ServletUtil.sendResponse(response, HttpServletResponse.SC_OK, DeactivateState.MEDIA_TYPE, deactivateStateString);
	}
}
