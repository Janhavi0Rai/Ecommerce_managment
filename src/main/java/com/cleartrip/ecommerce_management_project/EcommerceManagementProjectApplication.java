package com.cleartrip.ecommerce_management_project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.cleartrip.ecommerce_management_project.model")
@EnableJpaRepositories("com.cleartrip.ecommerce_management_project.repository")
public class EcommerceManagementProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcommerceManagementProjectApplication.class, args);
	}

}
