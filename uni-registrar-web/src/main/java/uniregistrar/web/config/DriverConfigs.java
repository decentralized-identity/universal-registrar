package uniregistrar.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
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

		public String getMethod() {
			return method;
		}

		public void setMethod(String value) {
			this.method = value;
		}

		public String getURL() {
			return url;
		}

		public void setURL(String value) {
			this.url = value;
		}

		public String getPropertiesEndpoint() {
			return propertiesEndpoint;
		}

		public void setPropertiesEndpoint(String value) {
			this.propertiesEndpoint = value;
		}

		@Override
		public String toString() {
			return new StringJoiner(", ", DriverConfig.class.getSimpleName() + "[", "]")
					.add("method='" + method + "'")
					.add("url='" + url + "'")
					.add("propertiesEndpoint='" + propertiesEndpoint + "'")
					.toString();
		}
	}
}

