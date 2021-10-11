package com.startup.goHappy.config;

import java.util.Properties;

import org.springframework.context.annotation.Bean;

public class JavaMailSenderImpl {
	@Bean
	public org.springframework.mail.javamail.JavaMailSenderImpl getJavaMailSender() {
	    org.springframework.mail.javamail.JavaMailSenderImpl mailSender = new org.springframework.mail.javamail.JavaMailSenderImpl();
	    mailSender.setHost("smtp.gmail.com");
	    mailSender.setPort(587);
	    
	    mailSender.setUsername("my.gmail@gmail.com");
	    mailSender.setPassword("password");
	    
	    Properties props = mailSender.getJavaMailProperties();
	    props.put("mail.transport.protocol", "smtp");
	    props.put("mail.smtp.auth", "true");
	    props.put("mail.smtp.starttls.enable", "true");
	    props.put("mail.debug", "true");
	    
	    return mailSender;
	}
}
