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

			if (log.isWarnEnabled()) log.warn("Create problem: " + ex.getMessage(), ex);
			WebUniRegistrar.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, "Create problem: " + ex.getMessage());
			return;
		}

		String method = request.getParameter("method");

		if (log.isInfoEnabled()) log.info("Incoming create request for method " + method + ": " + createRequest);

		if (createRequest == null) {

			WebUniRegistrar.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, null, "No create request found.");
			return;
		}

		// execute the request

		CreateState createState;
		String createStateString;

		try {

			createState = this.create(method, createRequest);
			createStateString = createState == null ? null : createState.toJson();
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Create problem for " + createRequest + ": " + ex.getMessage(), ex);
			WebUniRegistrar.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, "Create problem for " + createRequest + ": " + ex.getMessage());
			return;
		}

		if (log.isInfoEnabled()) log.info("Create state for " + createRequest + ": " + createStateString);

		// no create state?

		if (createStateString == null) {

			WebUniRegistrar.sendResponse(response, HttpServletResponse.SC_NOT_FOUND, null, "No create state for " + createRequest + ": " + createStateString);
			return;
		}

		// write create state

		WebUniRegistrar.sendResponse(response, HttpServletResponse.SC_OK, MIME_TYPE, createStateString);
	}
}