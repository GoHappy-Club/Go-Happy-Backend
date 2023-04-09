package com.startup.goHappy.integrations.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.alibaba.fastjson.JSONObject;
import com.razorpay.RazorpayException;
import com.startup.goHappy.integrations.service.RazorPayService;

@ContextConfiguration(classes = RazorPayController.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class RazorPayControllerTest {

    @InjectMocks
    RazorPayController razorPayController;

    @Mock
    RazorPayService razorPayService;

    @Mock
    MockMvc mockMvc;

    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSetup() throws Exception {
        JSONObject params = new JSONObject();
        params.put("amount", "1");
        when(razorPayService.pay(params.getString("amount"))).thenReturn("123");
        assertEquals("Success", razorPayController.setup(params));
    }

}
