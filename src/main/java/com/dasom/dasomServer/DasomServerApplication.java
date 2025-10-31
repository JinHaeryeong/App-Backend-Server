package com.dasom.dasomServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@MapperScan(basePackages = "com.dasom.dasomServer.dao")
@SpringBootApplication
public class DasomServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DasomServerApplication.class, args);
	}

}