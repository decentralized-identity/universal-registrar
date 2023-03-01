package uniregistrar.web.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniregistrar.UniRegistrar;
import uniregistrar.web.WebUniRegistrar;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class MethodsServlet extends WebUniRegistrar {

	protected static Logger log = LoggerFactory.getLogger(MethodsServlet.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// read request

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		if (log.isInfoEnabled()) log.info("Incoming request.");

		// execute the request

		Set<String> methods;
		String methodsString;

		try {

			methods = this.methods();
			methodsString = methods == null ? null : objectMapper.writeValueAsString(methods);
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Registrar reported: " + ex.getMessage(), ex);
			ServletUtil.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, "Registrar reported: " + ex.getMessage());
			return;
		}

		if (log.isInfoEnabled()) log.info("Methods: " + methods);

		// no result?

		if (methods == null) {

			ServletUtil.sendResponse(response, HttpServletResponse.SC_NOT_FOUND, null, "No methods.");
			return;
		}

		// write result

		ServletUtil.sendResponse(response, HttpServletResponse.SC_OK, UniRegistrar.METHODS_MIME_TYPE, methodsString);
	}
}