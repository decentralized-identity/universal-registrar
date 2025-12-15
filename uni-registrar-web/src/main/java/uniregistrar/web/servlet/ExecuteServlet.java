package uniregistrar.web.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniregistrar.RegistrationException;
import uniregistrar.RegistrationMediaTypes;
import uniregistrar.driver.util.HttpBindingServerUtil;
import uniregistrar.openapi.model.ExecuteRequest;
import uniregistrar.openapi.model.RegistrarState;
import uniregistrar.util.HttpBindingUtil;
import uniregistrar.web.WebUniRegistrar;

import java.io.IOException;
import java.util.Map;

public class ExecuteServlet extends WebUniRegistrar {

	private static final Logger log = LoggerFactory.getLogger(ExecuteServlet.class);

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// read request

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		final Map<String, Object> requestMap = readRequestMap("EXECUTE", request, response);
		if (requestMap == null) return;

		final String method = readMethod("EXECUTE", requestMap, request, response);
		if (method == null) return;

		// parse request

		ExecuteRequest executeRequest = WebUniRegistrar.parseRequest(method, "EXECUTE", requestMap, ExecuteRequest.class, response);
		if (executeRequest == null) return;

		// prepare options

		WebUniRegistrar.prepareOptions(request, executeRequest);

		// execute the request

		RegistrarState state = null;
		final Map<String, Object> stateMap;

		try {

			state = this.execute(method, executeRequest);
			if (state == null) throw new RegistrationException("No state.");
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("EXECUTE problem for " + executeRequest + ": " + ex.getMessage(), ex);

			if (! (ex instanceof RegistrationException)) ex = new RegistrationException("EXECUTE problem for " + executeRequest + ": " + ex.getMessage());
			state = ((RegistrationException) ex).toErrorRegistrarState();
		} finally {
			stateMap = state == null ? null : HttpBindingUtil.toMapState(state);
		}

		if (log.isInfoEnabled()) log.info("State for " + executeRequest + ": " + state);

		// write state

		ServletUtil.sendResponse(
				response,
				HttpBindingServerUtil.httpStatusCodeForState(state),
				RegistrationMediaTypes.STATE_MEDIA_TYPE,
				HttpBindingServerUtil.toHttpBodyStreamState(stateMap));
	}
}