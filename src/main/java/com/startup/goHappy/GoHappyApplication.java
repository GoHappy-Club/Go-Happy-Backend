package com.startup.goHappy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

@EnableEncryptableProperties
@SpringBootApplication
public class GoHappyApplication {

	public static void main(String[] args) {
		SpringApplication.run(GoHappyApplication.class, args);
	}

}
