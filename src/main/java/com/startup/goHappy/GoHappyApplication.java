package com.startup.goHappy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

@EnableEncryptableProperties
@SpringBootApplication
@EnableScheduling
public class GoHappyApplication {

	public static void main(String[] args) {
		try {
			SpringApplication.run(GoHappyApplication.class, args);
	    } catch (Throwable e) {
	        if(e.getClass().getName().contains("SilentExitException")) {
	            System.out.println("Spring is restarting the main thread - See spring-boot-devtools");
	        } else {
	        	System.out.println("Application crashed!"+ e);
	        }
	    }
	}

}
//Add the controller.
@RestController
class HelloWorldController {
@GetMapping("/")
public String hello() {
 return "hello world!";
}
}
