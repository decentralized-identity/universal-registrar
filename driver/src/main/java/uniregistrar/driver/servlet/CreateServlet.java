package uniregistrar.driver.servlet;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uniregistrar.request.CreateRequest;
import uniregistrar.state.CreateState;

public class CreateServlet extends AbstractServlet implements Servlet {

	private static final long serialVersionUID = 7431292074564723539L;

	private static Logger log = LoggerFactory.getLogger(CreateServlet.class);

	public CreateServlet() {

		super();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// read request

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		CreateRequest createRequest = CreateRequest.fromJson(request.getReader());

		if (log.isInfoEnabled()) log.info("Incoming create request: " + createRequest);

		if (createRequest == null) {

			sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, null, "No create request found.");
			return;
		}

		// invoke the driver

		CreateState createState;
		String createStateString;

		try {

			createState = InitServlet.getDriver().create(createRequest);
			createStateString = createState == null ? null : createState.toJson();
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Driver reported for " + createRequest + ": " + ex.getMessage(), ex);
			sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, "Driver reported for " + createRequest + ": " + ex.getMessage());
			return;
		}

		if (log.isInfoEnabled()) log.info("Create state for " + createRequest + ": " + createStateString);

		// no create state?

		if (createState == null) {

			sendResponse(response, HttpServletResponse.SC_NOT_FOUND, null, "No create state for " + createRequest);
			return;
		}

		// write create state

		sendResponse(response, HttpServletResponse.SC_OK, CreateState.MIME_TYPE, createStateString);
	}
}
