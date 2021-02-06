package uniregistrar.web.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniregistrar.request.CreateRequest;
import uniregistrar.state.CreateState;
import uniregistrar.web.WebUniRegistrar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CreateServlet extends WebUniRegistrar {

	private static final long serialVersionUID = 5659041840241560964L;

	protected static Logger log = LoggerFactory.getLogger(CreateServlet.class);

	public static final String MIME_TYPE = "application/json";

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// read request

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		CreateRequest createRequest;

		try {

			createRequest = CreateRequest.fromJson(request.getReader());
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Request problem: " + ex.getMessage(), ex);
			WebUniRegistrar.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, "Request problem: " + ex.getMessage());
			return;
		}

		String driverId = request.getParameter("driverId");

		if (log.isInfoEnabled()) log.info("Incoming register request for driver " + driverId + ": " + createRequest);

		if (createRequest == null) {

			WebUniRegistrar.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, null, "No register request found.");
			return;
		}

		// execute the request

		CreateState createState;
		String registerStateString;

		try {

			createState = this.create(driverId, createRequest);
			registerStateString = createState == null ? null : createState.toJson();
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Register problem for " + createRequest + ": " + ex.getMessage(), ex);
			WebUniRegistrar.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, "Register problem for " + createRequest + ": " + ex.getMessage());
			return;
		}

		if (log.isInfoEnabled()) log.info("Register state for " + createRequest + ": " + registerStateString);

		// no register state?

		if (registerStateString == null) {

			WebUniRegistrar.sendResponse(response, HttpServletResponse.SC_NOT_FOUND, null, "No register state for " + createRequest + ": " + registerStateString);
			return;
		}

		// write register state

		WebUniRegistrar.sendResponse(response, HttpServletResponse.SC_OK, MIME_TYPE, registerStateString);
	}
}