package com.dasom.dasomServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableScheduling
@MapperScan(basePackages = "com.dasom.dasomServer.DAO")
@EnableWebMvc
@SpringBootApplication
public class DasomServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DasomServerApplication.class, args);
	}
}