package com.dasom.dasomServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@MapperScan(basePackages = "com.dasom.dasomServer.DAO")
@SpringBootApplication
public class DasomServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DasomServerApplication.class, args);
	}
}