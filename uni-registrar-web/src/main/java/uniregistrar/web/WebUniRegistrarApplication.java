package uniregistrar.web;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import uniregistrar.local.LocalUniRegistrar;

@SpringBootApplication
public class WebUniRegistrarApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(WebUniRegistrarApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(WebUniRegistrarApplication.class);
    }

	@Bean(name = "UniRegistrar")
	public LocalUniRegistrar localUniRegistrar() {
		return new LocalUniRegistrar();
	}
}
