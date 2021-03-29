package uniregistrar.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uniregistrar.request.UpdateRequest;
import uniregistrar.state.UpdateState;
import uniregistrar.web.WebUniRegistrar;

public class UpdateServlet extends WebUniRegistrar {

	private static final long serialVersionUID = 5659041840241560964L;

	protected static Logger log = LoggerFactory.getLogger(UpdateServlet.class);

	public static final String MIME_TYPE = "application/json";

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// read request

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		UpdateRequest updateRequest;

		try {

			updateRequest = UpdateRequest.fromJson(request.getReader());
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Request problem: " + ex.getMessage(), ex);
			WebUniRegistrar.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, "Request problem: " + ex.getMessage());
			return;
		}

		String method = request.getParameter("method");

		if (log.isInfoEnabled()) log.info("Incoming update request for method " + method + ": " + updateRequest);

		if (updateRequest == null) {

			WebUniRegistrar.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, null, "No update request found.");
			return;
		}

		// execute the request

		UpdateState updateState;
		String updateStateString;

		try {

			updateState = this.update(method, updateRequest);
			updateStateString = updateState == null ? null : updateState.toJson();
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Update problem for " + updateRequest + ": " + ex.getMessage(), ex);
			WebUniRegistrar.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, "Update problem for " + updateRequest + ": " + ex.getMessage());
			return;
		}

		if (log.isInfoEnabled()) log.info("Update state for " + updateRequest + ": " + updateStateString);

		// no update state?

		if (updateStateString == null) {

			WebUniRegistrar.sendResponse(response, HttpServletResponse.SC_NOT_FOUND, null, "No update state for " + updateRequest + ".");
			return;
		}

		// write update state

		WebUniRegistrar.sendResponse(response, HttpServletResponse.SC_OK, MIME_TYPE, updateStateString);
	}
}