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

	public void setMethods(String methods) {
		this.methods = methods;
	}
}
