package uniregistrar.driver.servlet;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniregistrar.request.CreateRequest;
import uniregistrar.state.CreateState;

import java.io.IOException;

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

		if (log.isInfoEnabled()) log.info("Driver: Incoming create request: " + createRequest);

		if (createRequest == null) {

			sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, null, "Driver: No create request found.");
			return;
		}

		// invoke the driver

		CreateState createState;
		String createStateString;

		try {

			createState = InitServlet.getDriver().create(createRequest);
			createStateString = createState == null ? null : createState.toJson();
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Driver: Create problem for " + createRequest + ": " + ex.getMessage(), ex);
			sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, "Driver: Create problem: " + ex.getMessage());
			return;
		}

		if (log.isInfoEnabled()) log.info("Driver: Create state for " + createRequest + ": " + createStateString);

		// no create state?

		if (createState == null) {

			sendResponse(response, HttpServletResponse.SC_NOT_FOUND, null, "Driver: No create state.");
			return;
		}

		// write create state

		sendResponse(response, HttpServletResponse.SC_OK, CreateState.MEDIA_TYPE, createStateString);
	}
}
