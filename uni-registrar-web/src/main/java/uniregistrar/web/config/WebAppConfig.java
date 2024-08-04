package uniregistrar.web.config;

import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uniregistrar.driver.Driver;
import uniregistrar.driver.http.HttpDriver;
import uniregistrar.local.LocalUniRegistrar;
import uniregistrar.web.servlet.*;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class WebAppConfig {

	private static final Logger log = LogManager.getLogger(WebAppConfig.class);

	@Autowired
	private DriverConfigs driverConfigs;

    @Autowired
    private ServletMappings servletMappings;

	@Autowired
	private LocalUniRegistrar localUniRegistrar;

    @Bean(name = "CreateServlet")
    public CreateServlet createServlet(){
        return new CreateServlet();
    }

    @Bean
    public ServletRegistrationBean<CreateServlet> createServletRegistrationBean() {
        return new ServletRegistrationBean<>(createServlet(), fixWildcardPattern(servletMappings.getCreate()));
    }

    @Bean(name = "UpdateServlet")
    public UpdateServlet updateServlet(){
        return new UpdateServlet();
    }

    @Bean
    public ServletRegistrationBean<UpdateServlet> updateServletRegistrationBean() {
        return new ServletRegistrationBean<>(updateServlet(), fixWildcardPattern(servletMappings.getUpdate()));
    }

    @Bean(name = "DeactivateServlet")
    public DeactivateServlet deactivateServlet(){
        return new DeactivateServlet();
    }

    @Bean
    public ServletRegistrationBean<DeactivateServlet> deactivateServletRegistrationBean() {
        return new ServletRegistrationBean<>(deactivateServlet(), fixWildcardPattern(servletMappings.getDeactivate()));
    }

    @Bean(name = "PropertiesServlet")
    public PropertiesServlet propertiesServlet(){
        return new PropertiesServlet();
    }

    @Bean
    public ServletRegistrationBean<PropertiesServlet> propertiesServletRegistrationBean() {
        return new ServletRegistrationBean<>(propertiesServlet(), fixWildcardPattern(servletMappings.getProperties()));
    }
    @Bean(name = "MethodsServlet")
    public MethodsServlet methodsServlet() {
        return new MethodsServlet();
    }
    @Bean
    public ServletRegistrationBean<MethodsServlet> methodServletRegistrationBean() {
        return new ServletRegistrationBean<>(methodsServlet(), fixWildcardPattern(servletMappings.getMethods()));
    }

    public static String fixWildcardPattern(String s) {
        if(s == null) return "";
        if (s.endsWith("*")) return s;
        if (s.endsWith("/")) return s + "*";
        return s + "/*";
    }

    public static String normalizeUri(String s, boolean postSlash) {
        if (s == null) return null;
        String url = s;
        if (url.endsWith("*")) url = url.substring(0, url.length() - 1);

        URI uri = URI.create(url + "/").normalize();

        return postSlash ? uri.toString() : uri.toString().substring(0, uri.toString().length() - 1);
    }

	public void configureLocalUniRegistrar(DriverConfigs driverConfigs, LocalUniRegistrar uniRegistrar) {

		Map<String, Driver> drivers = new LinkedHashMap<>();

		for (DriverConfigs.DriverConfig dc : driverConfigs.getDrivers()) {

			String method = dc.getMethod();
			String url = dc.getUrl();
			String propertiesEndpoint = dc.getPropertiesEndpoint();
            String includeMethodParameter = dc.getIncludeMethodParameter();

			if (method == null) throw new IllegalArgumentException("Missing 'method' entry in driver configuration.");
			if (url == null) throw new IllegalArgumentException("Missing 'url' entry in driver configuration.");

			// construct HTTP driver

			HttpDriver driver = new HttpDriver();
            driver.setMethod(method);

			if (! url.endsWith("/")) url = url + "/";
			driver.setCreateUri(normalizeUri((url + servletMappings.getCreate()), false));
			driver.setUpdateUri(normalizeUri((url + servletMappings.getUpdate()), false));
			driver.setDeactivateUri(normalizeUri((url + servletMappings.getDeactivate()), false));
			if ("true".equals(propertiesEndpoint)) driver.setPropertiesUri(normalizeUri((url + servletMappings.getProperties()), false));
            if ("true".equals(includeMethodParameter)) driver.setIncludeMethodParameter(Boolean.valueOf(includeMethodParameter));

			// done

			drivers.put(method, driver);
			if (log.isInfoEnabled()) log.info("Added driver for method '" + method + "' at " + driver.getCreateUri() + " and " + driver.getUpdateUri() + " and " + driver.getDeactivateUri() + " (" + driver.getPropertiesUri() + ")" + " (" + driver.getIncludeMethodParameter() + ")");
		}

		uniRegistrar.setDrivers(drivers);
	}

	@PostConstruct
	private void initDrivers() {
		if (driverConfigs.getDrivers() != null) configureLocalUniRegistrar(driverConfigs, localUniRegistrar);
	}

}
