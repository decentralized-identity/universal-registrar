package uniregistrar.driver.servlet;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uniregistrar.request.RegisterRequest;
import uniregistrar.state.RegisterState;

public class RegisterServlet extends AbstractServlet implements Servlet {

	private static final long serialVersionUID = 7431292074564723539L;

	private static Logger log = LoggerFactory.getLogger(RegisterServlet.class);

	public RegisterServlet() {

		super();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// read request

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		RegisterRequest registerRequest = RegisterRequest.fromJson(request.getReader());

		if (log.isInfoEnabled()) log.info("Incoming register request: " + registerRequest);

		if (registerRequest == null) {

			sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, null, "No register request found.");
			return;
		}

		// invoke the driver

		RegisterState registerState;
		String registerStateString;

		try {

			registerState = InitServlet.getDriver().register(registerRequest);
			registerStateString = registerState == null ? null : registerState.toJson();
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Driver reported for " + registerRequest + ": " + ex.getMessage(), ex);
			sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, "Driver reported for " + registerRequest + ": " + ex.getMessage());
			return;
		}

		if (log.isInfoEnabled()) log.info("Register state for " + registerRequest + ": " + registerStateString);

		// no register state?

		if (registerState == null) {

			sendResponse(response, HttpServletResponse.SC_NOT_FOUND, null, "No register state for " + registerRequest);
			return;
		}

		// write register state

		sendResponse(response, HttpServletResponse.SC_OK, RegisterState.MIME_TYPE, registerStateString);
	}
}
