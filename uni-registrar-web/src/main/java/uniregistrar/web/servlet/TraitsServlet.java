package uniregistrar.web.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniregistrar.UniRegistrar;
import uniregistrar.web.WebUniRegistrar;

import java.io.IOException;
import java.util.Map;

public class TraitsServlet extends WebUniRegistrar {

	protected static final Logger log = LoggerFactory.getLogger(TraitsServlet.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// read request

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		if (log.isInfoEnabled()) log.info("Incoming request.");

		// execute the request

		Map<String, Map<String, Object>> traits;
		String traitsString;

		try {

			traits = this.traits();
			traitsString = traits == null ? null : objectMapper.writeValueAsString(traits);
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Registrar reported: " + ex.getMessage(), ex);
			ServletUtil.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Registrar reported: " + ex.getMessage());
			return;
		}

		if (log.isInfoEnabled()) log.info("Traits: " + traits);

		// no result?

		if (traits == null) {

			ServletUtil.sendResponse(response, HttpServletResponse.SC_NOT_FOUND, "No traits.");
			return;
		}

		// write result

		ServletUtil.sendResponse(response, HttpServletResponse.SC_OK, UniRegistrar.TRAITS_MIME_TYPE, traitsString);
	}
}