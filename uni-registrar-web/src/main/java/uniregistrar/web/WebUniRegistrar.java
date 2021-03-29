package uniregistrar.web;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.HttpRequestHandler;

import uniregistrar.RegistrationException;
import uniregistrar.UniRegistrar;
import uniregistrar.request.DeactivateRequest;
import uniregistrar.request.CreateRequest;
import uniregistrar.request.UpdateRequest;
import uniregistrar.state.DeactivateState;
import uniregistrar.state.CreateState;
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
	public CreateState create(String method, CreateRequest createRequest) throws RegistrationException {

		return this.getUniRegistrar() == null ? null : this.getUniRegistrar().create(method, createRequest);
	}

	@Override
	public UpdateState update(String method, UpdateRequest updateRequest) throws RegistrationException {

		return this.getUniRegistrar() == null ? null : this.getUniRegistrar().update(method, updateRequest);
	}

	@Override
	public DeactivateState deactivate(String method, DeactivateRequest deactivateRequest) throws RegistrationException {

		return this.getUniRegistrar() == null ? null : this.getUniRegistrar().deactivate(method, deactivateRequest);
	}

	@Override
	public Map<String, Map<String, Object>> properties() throws RegistrationException {

		return this.getUniRegistrar() == null ? null : this.getUniRegistrar().properties();
	}

	@Override
	public Set<String> methods() throws RegistrationException {

		return this.getUniRegistrar() == null ? null : this.getUniRegistrar().methods();
	}

	/*
	 * Helper methods
	 */

	protected static void sendResponse(HttpServletResponse response, int status, String contentType, Object body) throws IOException {

		response.setStatus(status);

		if (contentType != null) response.setContentType(contentType);

		response.setHeader("Access-Control-Allow-Origin", "*");

		if (body instanceof String) {

			PrintWriter printWriter = response.getWriter();
			printWriter.write((String) body);
			printWriter.flush();
			printWriter.close();
		} else if (body instanceof byte[]) {

			OutputStream outputStream = response.getOutputStream();
			outputStream.write((byte[]) body);
			outputStream.flush();
			outputStream.close();
		} else {

			PrintWriter printWriter = response.getWriter();
			printWriter.write(body.toString());
			printWriter.flush();
			printWriter.close();
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
