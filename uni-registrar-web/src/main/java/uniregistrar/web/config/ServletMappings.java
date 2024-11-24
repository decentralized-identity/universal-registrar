package uniregistrar.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("server.servlet.mappings")
public class ServletMappings {

	private String properties;
	private String create;
	private String update;
	private String deactivate;
	private String execute;
	private String createResource;
	private String updateResource;
	private String deactivateResource;
	private String methods;

	public String getProperties() {
		return properties;
	}

	public String getCreate() {
		return create;
	}

	public String getUpdate() {
		return update;
	}

	public String getDeactivate() {
		return deactivate;
	}

	public String getExecute() {
		return execute;
	}

	public String getCreateResource() {
		return createResource;
	}

	public String getUpdateResource() {
		return updateResource;
	}

	public String getDeactivateResource() {
		return deactivateResource;
	}

	public String getMethods() {
		return methods;
	}

	public void setProperties(String properties) {
		this.properties = properties;
	}

	public void setCreate(String create) {
		this.create = create;
	}

	public void setUpdate(String update) {
		this.update = update;
	}

	public void setDeactivate(String deactivate) {
		this.deactivate = deactivate;
	}

	public void setExecute(String execute) {
		this.execute = execute;
	}

	public void setCreateResource(String createResource) {
		this.createResource = createResource;
	}

	public void setUpdateResource(String updateResource) {
		this.updateResource = updateResource;
	}

	public void setDeactivateResource(String deactivateResource) {
		this.deactivateResource = deactivateResource;
	}

	public void setMethods(String methods) {
		this.methods = methods;
	}
}
