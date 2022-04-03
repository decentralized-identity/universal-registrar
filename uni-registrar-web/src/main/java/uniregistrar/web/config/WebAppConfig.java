package uniregistrar.web.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uniregistrar.local.LocalUniRegistrar;
import uniregistrar.local.extensions.Extension;
import uniregistrar.local.extensions.impl.DummyExtension;
import uniregistrar.web.servlet.CreateServlet;
import uniregistrar.web.servlet.DeactivateServlet;
import uniregistrar.web.servlet.PropertiesServlet;
import uniregistrar.web.servlet.UpdateServlet;
import uniregistrar.web.servlet.MethodsServlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebAppConfig {

    @Bean(name = "UniRegistrar")
    public LocalUniRegistrar localUniRegistrar() throws IOException {
        return LocalUniRegistrar.fromConfigFile("./config.json");
    }

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

}
