package com.startup.goHappy.integrations.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Component
public class EmailService {
	
	@Autowired
    private JavaMailSender emailSender;

    public void sendSimpleMessage(String to, String subject, String text) throws MessagingException {
        
//        SimpleMailMessage message = new SimpleMailMessage(); 
//        message.setFrom("noreply@gohappy.com");
//        message.setTo(to); 
//        message.setSubject(subject); 
//        message.setText(text,true);
//        emailSender.send(message);
    	MimeMessage msg = emailSender.createMimeMessage();

        // true = multipart message
        MimeMessageHelper helper = new MimeMessageHelper(msg, true);
		
        helper.setTo(to);

        helper.setSubject(subject);

        // default = text/plain
        //helper.setText("Check attachment for image!");

        // true = text/html
        helper.setText(text, true);

		// hard coded a file path
        //FileSystemResource file = new FileSystemResource(new File("path/android.png"));

//        helper.addAttachment("my_photo.png", new ClassPathResource("android.png"));

        emailSender.send(msg);
        
    }
	void sendEmailWithAttachment() throws MessagingException{

        MimeMessage msg = emailSender.createMimeMessage();

        // true = multipart message
        MimeMessageHelper helper = new MimeMessageHelper(msg, true);
		
        helper.setTo("to_@email");

        helper.setSubject("Testing from Spring Boot");

        // default = text/plain
        //helper.setText("Check attachment for image!");

        // true = text/html
        helper.setText("<h1>Check attachment for image!</h1>", true);

		// hard coded a file path
        //FileSystemResource file = new FileSystemResource(new File("path/android.png"));

        helper.addAttachment("my_photo.png", new ClassPathResource("android.png"));

        emailSender.send(msg);

    }
}


	