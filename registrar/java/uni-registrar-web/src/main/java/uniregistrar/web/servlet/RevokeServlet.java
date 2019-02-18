package uniregistrar.web.servlet;

import java.io.IOException;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uniregistrar.RegistrationException;
import uniregistrar.request.RevokeRequest;
import uniregistrar.state.RevokeState;
import uniregistrar.web.WebUniRegistrar;

public class RevokeServlet extends WebUniRegistrar {

	private static final long serialVersionUID = 5659041840241560964L;

	protected static Logger log = LoggerFactory.getLogger(RevokeServlet.class);

	public static final String MIME_TYPE = "application/json";

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// read request

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		String driverId = URLDecoder.decode(request.getQueryString(), "UTF-8");

		RevokeRequest revokeRequest;

		try {

			revokeRequest = RevokeRequest.fromJson(request.getReader());
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Request problem: " + ex.getMessage(), ex);
			WebUniRegistrar.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, "Request problem: " + ex.getMessage());
			return;
		}

		if (log.isInfoEnabled()) log.info("Incoming revoke request for driver " + driverId + ": " + revokeRequest);

		if (revokeRequest == null) {

			WebUniRegistrar.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, null, "No revoke request found.");
			return;
		}

		// execute the request

		RevokeState revokeState;
		String revokeStateString;

		try {

			revokeState = this.revoke(driverId, revokeRequest);
			revokeStateString = revokeState == null ? null : revokeState.toJson();
		} catch (RegistrationException ex) {

			if (log.isWarnEnabled()) log.warn("Revoke problem for " + revokeRequest + ": " + ex.getMessage(), ex);
			WebUniRegistrar.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, "Revoke problem for " + revokeRequest + ": " + ex.getMessage());
			return;
		}

		if (log.isInfoEnabled()) log.info("Revoke state for " + revokeRequest + ": " + revokeStateString);

		// no revoke state?

		if (revokeStateString == null) {

			WebUniRegistrar.sendResponse(response, HttpServletResponse.SC_NOT_FOUND, null, "No revoke state for " + revokeRequest + ".");
			return;
		}

		// write revoke state

		WebUniRegistrar.sendResponse(response, HttpServletResponse.SC_OK, MIME_TYPE, revokeStateString);
	}
}