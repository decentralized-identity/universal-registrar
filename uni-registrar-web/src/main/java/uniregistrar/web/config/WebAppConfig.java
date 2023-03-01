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
import uniregistrar.local.extensions.Extension;
import uniregistrar.local.extensions.impl.DummyExtension;
import uniregistrar.web.servlet.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class WebAppConfig {

	private static final Logger log = LogManager.getLogger(WebAppConfig.class);

	@Autowired
	private DriverConfigs driverConfigs;

	@Autowired
	private LocalUniRegistrar localUniRegistrar;

    @Bean
    public List<Extension> extensions(){
        List<Extension> list = new ArrayList<>();
        list.add(new DummyExtension());
        return list;
    }

    @Bean(name = "CreateServlet")
    public CreateServlet createServlet(){
        return new CreateServlet();
    }

    @Bean
    public ServletRegistrationBean<CreateServlet> createServletRegistrationBean() {
        return new ServletRegistrationBean<>(createServlet(), "/1.0/create/*");
    }

    @Bean(name = "UpdateServlet")
    public UpdateServlet updateServlet(){
        return new UpdateServlet();
    }

    @Bean
    public ServletRegistrationBean<UpdateServlet> updateServletRegistrationBean() {
        return new ServletRegistrationBean<>(updateServlet(), "/1.0/update/*");
    }

    @Bean(name = "DeactivateServlet")
    public DeactivateServlet deactivateServlet(){
        return new DeactivateServlet();
    }

    @Bean
    public ServletRegistrationBean<DeactivateServlet> deactivateServletRegistrationBean() {
        return new ServletRegistrationBean<>(deactivateServlet(), "/1.0/deactivate/*");
    }

    @Bean(name = "PropertiesServlet")
    public PropertiesServlet propertiesServlet(){
        return new PropertiesServlet();
    }

    @Bean
    public ServletRegistrationBean<PropertiesServlet> propertiesServletRegistrationBean() {
        return new ServletRegistrationBean<>(propertiesServlet(), "/1.0/properties/*");
    }
    @Bean(name = "MethodsServlet")
    public MethodsServlet methodsServlet() {
        return new MethodsServlet();
    }
    @Bean
    public ServletRegistrationBean<MethodsServlet> methodServletRegistrationBean() {
        return new ServletRegistrationBean<>(methodsServlet(), "/1.0/methods/*");
    }

	public void configureLocalUniRegistrar(DriverConfigs driverConfigs, LocalUniRegistrar uniRegistrar) {

		Map<String, Driver> drivers = new LinkedHashMap<>();

		for (DriverConfigs.DriverConfig dc : driverConfigs.getDrivers()) {

			String method = dc.getMethod();
			String url = dc.getURL();
			String propertiesEndpoint = dc.getPropertiesEndpoint();

			if (method == null)
				throw new IllegalArgumentException("Missing 'method' entry in driver configuration.");
			if (url == null) throw new IllegalArgumentException("Missing 'url' entry in driver configuration.");

			// construct HTTP driver

			HttpDriver driver = new HttpDriver();

			if (!url.endsWith("/")) url = url + "/";

			driver.setCreateUri(url + "1.0/create");
			driver.setUpdateUri(url + "1.0/update");
			driver.setDeactivateUri(url + "1.0/deactivate");
			if ("true".equals(propertiesEndpoint)) driver.setPropertiesUri(url + "1.0/properties");

			// done

			drivers.put(method, driver);
			if (log.isInfoEnabled())
				log.info("Added driver for method '" + method + "' at " + driver.getCreateUri() + " and " + driver.getUpdateUri() + " and " + driver.getDeactivateUri() + " (" + driver.getPropertiesUri() + ")");
		}

		uniRegistrar.setDrivers(drivers);
	}

	@PostConstruct
	private void initDrivers() {
		if (driverConfigs.getDrivers() != null) configureLocalUniRegistrar(driverConfigs, localUniRegistrar);
	}

}
