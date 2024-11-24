package uniregistrar.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@Configuration
@ConfigurationProperties("uniregistrar")
public class DriverConfigs {

	private List<DriverConfig> drivers;

	public List<DriverConfig> getDrivers() {
		return drivers;
	}

	public void setDrivers(List<DriverConfig> drivers) {
		this.drivers = drivers;
	}

	public static class DriverConfig {

		private String method;
		private String url;
		private String propertiesEndpoint;
		private String includeMethodParameter;
		private Map<String, Object> traits;

		public String getMethod() {
			return method;
		}

		public void setMethod(String value) {
			this.method = value;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String value) {
			this.url = value;
		}

		public String getPropertiesEndpoint() {
			return propertiesEndpoint;
		}

		public void setPropertiesEndpoint(String value) {
			this.propertiesEndpoint = value;
		}

		public String getIncludeMethodParameter() {
			return includeMethodParameter;
		}

		public void setIncludeMethodParameter(String includeMethodParameter) {
			this.includeMethodParameter = includeMethodParameter;
		}

		public Map<String,Object> getTraits() {
			return traits;
		}

		public void setTraits(Map<String,Object> traits) {
			this.traits = traits;
		}

		@Override
		public String toString() {
			return "DriverConfig{" +
					"method='" + method + '\'' +
					", url='" + url + '\'' +
					", propertiesEndpoint='" + propertiesEndpoint + '\'' +
					", includeMethodParameter='" + includeMethodParameter + '\'' +
					", traits=" + traits +
					'}';
		}
	}
}

