package com.epam.gymapp;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication
//@EnableDiscoveryClient
@EnableJms
public class GymappApplication {
	public static void main(String[] args) {
		SpringApplication.run(GymappApplication.class, args);
	}

}
