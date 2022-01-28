package uniregistrar.web.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniregistrar.RegistrationException;
import uniregistrar.driver.util.HttpBindingServerUtil;
import uniregistrar.request.CreateRequest;
import uniregistrar.state.State;
import uniregistrar.web.WebUniRegistrar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CreateServlet extends WebUniRegistrar {

	protected static Logger log = LoggerFactory.getLogger(CreateServlet.class);

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
			ServletUtil.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, "Create problem: " + ex.getMessage());
			return;
		}

		String method = request.getParameter("method");

		if (log.isInfoEnabled()) log.info("Incoming create request for method " + method + ": " + createRequest);

		if (createRequest == null) {

			ServletUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, null, "No create request found.");
			return;
		}

		// execute the request

		State state;

		try {

			state = this.create(method, createRequest);
			if (state == null) throw new RegistrationException("No state.");
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Create problem for " + createRequest + ": " + ex.getMessage(), ex);

			if (! (ex instanceof RegistrationException)) ex = new RegistrationException("Create problem for " + createRequest + ": " + ex.getMessage());
			state = ((RegistrationException) ex).toFailedState();
		}

		if (log.isInfoEnabled()) log.info("State for " + createRequest + ": " + state);

		// write state

		ServletUtil.sendResponse(
				response,
				HttpBindingServerUtil.httpStatusCodeForState(state),
				State.MEDIA_TYPE,
				HttpBindingServerUtil.toHttpBodyStreamState(state));
	}
}