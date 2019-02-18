package uniregistrar.driver.servlet;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uniregistrar.RegistrationException;
import uniregistrar.request.RevokeRequest;
import uniregistrar.state.RevokeState;

public class RevokeServlet extends AbstractServlet implements Servlet {

	private static final long serialVersionUID = 8532462131637520098L;

	private static Logger log = LoggerFactory.getLogger(RevokeServlet.class);

	public RevokeServlet() {

		super();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// read request

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		RevokeRequest revokeRequest = RevokeRequest.fromJson(request.getReader());

		if (log.isInfoEnabled()) log.info("Incoming revoke request: " + revokeRequest);

		if (revokeRequest == null) {

			sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, null, "No revoke request found.");
			return;
		}

		// invoke the driver

		RevokeState revokeState;
		String revokeStateString;

		try {

			revokeState = InitServlet.getDriver().revoke(revokeRequest);
			revokeStateString = revokeState == null ? null : revokeState.toJson();
		} catch (RegistrationException ex) {

			if (log.isWarnEnabled()) log.warn("Driver reported for " + revokeRequest + ": " + ex.getMessage(), ex);
			sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, "Driver reported for " + revokeRequest + ": " + ex.getMessage());
			return;
		}

		if (log.isInfoEnabled()) log.info("Revoke state for " + revokeRequest + ": " + revokeStateString);

		// no revoke state?

		if (revokeState == null) {

			sendResponse(response, HttpServletResponse.SC_NOT_FOUND, null, "No revoke state for " + revokeRequest);
			return;
		}

		// write revoke state

		sendResponse(response, HttpServletResponse.SC_OK, RevokeState.MIME_TYPE, revokeStateString);
	}
}
