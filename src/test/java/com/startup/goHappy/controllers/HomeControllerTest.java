package com.startup.goHappy.controllers;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.startup.goHappy.integrations.service.EmailService;

import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class HomeControllerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private HomeController homeController;

    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetEvents() throws Exception {
        homeController.getEvents();
        verify(emailService, times(1)).sendSimpleMessage(anyString(), anyString(), anyString());
    }
}
