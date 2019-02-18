package uniregistrar.web.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import uniregistrar.RegistrationException;
import uniregistrar.UniRegistrar;
import uniregistrar.web.WebUniRegistrar;

public class PropertiesServlet extends WebUniRegistrar {

	private static final long serialVersionUID = -8654511038420444615L;

	protected static Logger log = LoggerFactory.getLogger(PropertiesServlet.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// read request

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		if (log.isInfoEnabled()) log.info("Incoming request.");

		// execute the request

		Map<String, Map<String, Object>> properties;
		String propertiesString;

		try {

			properties = this.properties();
			propertiesString = properties == null ? null : objectMapper.writeValueAsString(properties);
		} catch (RegistrationException ex) {

			if (log.isWarnEnabled()) log.warn("Driver reported: " + ex.getMessage(), ex);
			WebUniRegistrar.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, "Driver reported: " + ex.getMessage());
			return;
		}

		if (log.isInfoEnabled()) log.info("Properties: " + properties);

		// no result?

		if (properties == null) {

			WebUniRegistrar.sendResponse(response, HttpServletResponse.SC_NOT_FOUND, null, "No properties.");
			return;
		}

		// write result

		WebUniRegistrar.sendResponse(response, HttpServletResponse.SC_OK, UniRegistrar.PROPERTIES_MIME_TYPE, propertiesString);
	}
}