package uniregistrar.driver.servlet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniregistrar.RegistrationMediaTypes;
import uniregistrar.openapi.RFC3339DateFormat;
import uniregistrar.openapi.model.CreateRequest;
import uniregistrar.openapi.model.CreateState;

import java.io.IOException;

public class CreateServlet extends HttpServlet implements Servlet {

	private static final Logger log = LoggerFactory.getLogger(CreateServlet.class);

	private static final ObjectMapper objectMapper = JsonMapper.builder()
			.serializationInclusion(JsonInclude.Include.NON_NULL)
			.disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
			.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.enable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE)
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
			.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
			.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
			.defaultDateFormat(new RFC3339DateFormat())
			.addModule(new JavaTimeModule())
			.build();

	public CreateServlet() {

		super();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// read request

		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		CreateRequest createRequest = objectMapper.readValue(request.getReader(), CreateRequest.class);

		if (log.isInfoEnabled()) log.info("Driver: Incoming create request: " + createRequest);

		if (createRequest == null) {

			ServletUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Driver: No create request found.");
			return;
		}

		// invoke the driver

		CreateState createState;
		String createStateString;

		try {

			createState = InitServlet.getDriver().create(createRequest);
			createStateString = createState == null ? null : objectMapper.writeValueAsString(createState);
		} catch (Exception ex) {

			if (log.isWarnEnabled()) log.warn("Driver: Create problem for " + createRequest + ": " + ex.getMessage(), ex);
			ServletUtil.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Driver: Create problem: " + ex.getMessage());
			return;
		}

		if (log.isInfoEnabled()) log.info("Driver: Create state for " + createRequest + ": " + createStateString);

		// no create state?

		if (createState == null) {

			ServletUtil.sendResponse(response, HttpServletResponse.SC_NOT_FOUND, "Driver: No create state.");
			return;
		}

		// write create state

		ServletUtil.sendResponse(response, HttpServletResponse.SC_OK, RegistrationMediaTypes.STATE_MEDIA_TYPE, createStateString);
	}
}
