package uniregistrar.driver.servlet;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniregistrar.RegistrationMediaTypes;
import uniregistrar.openapi.model.UpdateRequest;
import uniregistrar.openapi.model.UpdateState;
import uniregistrar.util.HttpBindingUtil;

import java.io.IOException;

public class UpdateServlet extends HttpServlet implements Servlet {

	private static final Logger log = LoggerFactory.getLogger(UpdateServlet.class);

	public UpdateServlet() {

		super();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// read request

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		UpdateRequest updateRequest = HttpBindingUtil.fromHttpBodyRequest(request.getReader(), UpdateRequest.class);

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
			updateStateString = updateState == null ? null : HttpBindingUtil.toHttpBodyState(updateState);
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

		ServletUtil.sendResponse(response, HttpServletResponse.SC_OK, RegistrationMediaTypes.STATE_MEDIA_TYPE, updateStateString);
	}
}
