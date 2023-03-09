package com.startup.goHappy.integrations.service;

import com.alibaba.fastjson.JSONObject;
import org.jetbrains.annotations.TestOnly;

import static org.mockito.ArgumentMatchers.any;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.util.ReflectionTestUtils;

import javax.mail.MessagingException;
import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

public class EmailserviceTest {

    EmailService  emailService;

    @Mock
    JavaMailSender emailSender;

    @BeforeEach
    public void setup(){
        emailService = new EmailService(emailSender);
        MockitoAnnotations.openMocks(this);
        //ReflectionTestUtils.setField(emailService, "user", "testUser");
        ReflectionTestUtils.setField(emailService, "service", null);
    }

    @Test
    public void shouldsendEmailCloudFunction() throws MessagingException, GeneralSecurityException, IOException {
        String to = "to_@email";
        String subject = "Testing from unit";
        String text = "Test user_example@example.com";
        emailService.sendSimpleMessage(to, subject, text);
    }
}
