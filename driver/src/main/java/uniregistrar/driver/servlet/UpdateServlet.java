package uniregistrar.driver.servlet;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniregistrar.request.UpdateRequest;
import uniregistrar.state.UpdateState;

import java.io.IOException;

public class UpdateServlet extends HttpServlet implements Servlet {

	private static Logger log = LoggerFactory.getLogger(UpdateServlet.class);

	public UpdateServlet() {

		super();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// read request

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		UpdateRequest updateRequest = UpdateRequest.fromJson(request.getReader());

		if (log.isInfoEnabled()) log.info("Driver: Incoming update request: " + updateRequest);

		if (updateRequest == null) {

			ServletUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Driver: No update request found.");
			return;
		}

		// invoke the driver

		UpdateState updateState;
		String updateStateString;

		try {

			updateState = InitServlet.getDriver().update(updateRequest);
			updateStateString = updateState == null ? null : updateState.toJson();
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Driver: Update problem for " + updateRequest + ": " + ex.getMessage(), ex);
			ServletUtil.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Driver: Update problem: " + ex.getMessage());
			return;
		}

		if (log.isInfoEnabled()) log.info("Driver: Update state for " + updateRequest + ": " + updateStateString);

		// no update state?

		if (updateState == null) {

			ServletUtil.sendResponse(response, HttpServletResponse.SC_NOT_FOUND, "Driver: No update state.");
			return;
		}

		// write update state

		ServletUtil.sendResponse(response, HttpServletResponse.SC_OK, UpdateState.MEDIA_TYPE, updateStateString);
	}
}
