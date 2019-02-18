package uniregistrar.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.HttpRequestHandler;

import uniregistrar.RegistrationException;
import uniregistrar.UniRegistrar;
import uniregistrar.request.RegisterRequest;
import uniregistrar.request.RevokeRequest;
import uniregistrar.request.UpdateRequest;
import uniregistrar.state.RegisterState;
import uniregistrar.state.RevokeState;
import uniregistrar.state.UpdateState;

public abstract class WebUniRegistrar extends HttpServlet implements HttpRequestHandler, UniRegistrar {

	private static final long serialVersionUID = -8314214552475026363L;

	private UniRegistrar uniRegistrar;

	public WebUniRegistrar() {

		super();
	}

	@Override
	public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		if ("GET".equals(request.getMethod())) this.doGet(request, response);
		if ("POST".equals(request.getMethod())) this.doPost(request, response);
		if ("PUT".equals(request.getMethod())) this.doPut(request, response);
		if ("DELETE".equals(request.getMethod())) this.doDelete(request, response);
		if ("OPTIONS".equals(request.getMethod())) this.doOptions(request, response);
	}

	@Override
	protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Headers", "Content-Type");
		response.setStatus(HttpServletResponse.SC_OK);
	}

	@Override
	public RegisterState register(String driverId, RegisterRequest registerRequest) throws RegistrationException {

		return this.getUniRegistrar() == null ? null : this.getUniRegistrar().register(driverId, registerRequest);
	}

	@Override
	public UpdateState update(String driverId, UpdateRequest updateRequest) throws RegistrationException {

		return this.getUniRegistrar() == null ? null : this.getUniRegistrar().update(driverId, updateRequest);
	}

	@Override
	public RevokeState revoke(String driverId, RevokeRequest revokeRequest) throws RegistrationException {

		return this.getUniRegistrar() == null ? null : this.getUniRegistrar().revoke(driverId, revokeRequest);
	}

	@Override
	public Map<String, Map<String, Object>> properties() throws RegistrationException {

		return this.getUniRegistrar() == null ? null : this.getUniRegistrar().properties();
	}

	/*
	 * Helper methods
	 */

	protected static void sendResponse(HttpServletResponse response, int status, String contentType, String body) throws IOException {

		response.setStatus(status);

		if (contentType != null) response.setContentType(contentType);

		response.setHeader("Access-Control-Allow-Origin", "*");

		if (body != null) {

			PrintWriter writer = response.getWriter();
			writer.write(body);
			writer.flush();
			writer.close();
		}
	}

	/*
	 * Getters and setters
	 */

	public UniRegistrar getUniRegistrar() {

		return this.uniRegistrar;
	}

	public void setUniRegistrar(UniRegistrar uniRegistrar) {

		this.uniRegistrar = uniRegistrar;
	}
}
