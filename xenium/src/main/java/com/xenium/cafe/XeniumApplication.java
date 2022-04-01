package com.xenium.cafe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class XeniumApplication extends SpringBootServletInitializer {

	@Override
	protected  SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(XeniumApplication.class);
    } 
	
	public static void main(String[] args) {
		SpringApplication.run(XeniumApplication.class, args);
	}

}
