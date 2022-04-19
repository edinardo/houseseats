package com.sibilante.houseseats;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

import com.sibilante.houseseats.resource.IgnoreShowResource;

@ServletComponentScan
@SpringBootApplication(scanBasePackageClasses = IgnoreShowResource.class)
public class RESTApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(RESTApplication.class, args);
	}

}
