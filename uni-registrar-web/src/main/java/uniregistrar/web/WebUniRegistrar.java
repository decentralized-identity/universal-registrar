package uniregistrar.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.web.HttpRequestHandler;
import uniregistrar.RegistrationException;
import uniregistrar.UniRegistrar;
import uniregistrar.openapi.model.*;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@WebServlet
public abstract class WebUniRegistrar extends HttpServlet implements HttpRequestHandler, UniRegistrar {

	@Autowired
	@Qualifier("UniRegistrar")
	private UniRegistrar uniRegistrar;

	public WebUniRegistrar() {

		super();
	}

	@Override
	public void handleRequest(HttpServletRequest request, @NonNull HttpServletResponse response) throws ServletException, IOException {
		if ("GET".equals(request.getMethod())) this.doGet(request, response);
		if ("POST".equals(request.getMethod())) this.doPost(request, response);
		if ("PUT".equals(request.getMethod())) this.doPut(request, response);
		if ("DELETE".equals(request.getMethod())) this.doDelete(request, response);
		if ("OPTIONS".equals(request.getMethod())) this.doOptions(request, response);
	}

	@Override
	protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Headers", "Accept, Content-Type");
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
	public ExecuteState execute(String method, ExecuteRequest executeRequest) throws RegistrationException {
		return this.getUniRegistrar() == null ? null : this.getUniRegistrar().execute(method, executeRequest);
	}

	@Override
	public CreateResourceState createResource(String method, CreateResourceRequest createResourceRequest) throws RegistrationException {
		return this.getUniRegistrar() == null ? null : this.getUniRegistrar().createResource(method, createResourceRequest);
	}

	@Override
	public UpdateResourceState updateResource(String method, UpdateResourceRequest updateResourceRequest) throws RegistrationException {
		return this.getUniRegistrar() == null ? null : this.getUniRegistrar().updateResource(method, updateResourceRequest);
	}

	@Override
	public DeactivateResourceState deactivateResource(String method, DeactivateResourceRequest deactivateResourceRequest) throws RegistrationException {
		return this.getUniRegistrar() == null ? null : this.getUniRegistrar().deactivateResource(method, deactivateResourceRequest);
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
	 * Getters and setters
	 */

	public UniRegistrar getUniRegistrar() {
		return this.uniRegistrar;
	}

	public void setUniRegistrar(UniRegistrar uniRegistrar) {
		this.uniRegistrar = uniRegistrar;
	}
}
