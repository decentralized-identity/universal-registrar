package uniregistrar.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uniregistrar.request.RegisterRequest;
import uniregistrar.state.RegisterState;
import uniregistrar.web.WebUniRegistrar;

public class RegisterServlet extends WebUniRegistrar {

	private static final long serialVersionUID = 5659041840241560964L;

	protected static Logger log = LoggerFactory.getLogger(RegisterServlet.class);

	public static final String MIME_TYPE = "application/json";

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// read request

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		String driverId = request.getParameter("driverId");

		RegisterRequest registerRequest;

		try {

			registerRequest = RegisterRequest.fromJson(request.getReader());
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Request problem: " + ex.getMessage(), ex);
			WebUniRegistrar.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, "Request problem: " + ex.getMessage());
			return;
		}

		if (log.isInfoEnabled()) log.info("Incoming register request for driver " + driverId + ": " + registerRequest);

		if (registerRequest == null) {

			WebUniRegistrar.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, null, "No register request found.");
			return;
		}

		// execute the request

		RegisterState registerState;
		String registerStateString;

		try {

			registerState = this.register(driverId, registerRequest);
			registerStateString = registerState == null ? null : registerState.toJson();
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Register problem for " + registerRequest + ": " + ex.getMessage(), ex);
			WebUniRegistrar.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, "Register problem for " + registerRequest + ": " + ex.getMessage());
			return;
		}

		if (log.isInfoEnabled()) log.info("Register state for " + registerRequest + ": " + registerStateString);

		// no register state?

		if (registerStateString == null) {

			WebUniRegistrar.sendResponse(response, HttpServletResponse.SC_NOT_FOUND, null, "No register state for " + registerRequest + ".");
			return;
		}

		// write register state

		WebUniRegistrar.sendResponse(response, HttpServletResponse.SC_OK, MIME_TYPE, registerStateString);
	}
}