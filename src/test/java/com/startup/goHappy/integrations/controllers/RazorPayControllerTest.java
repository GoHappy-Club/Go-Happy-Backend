package com.startup.goHappy.integrations.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import com.startup.goHappy.integrations.controller.RazorPayController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.alibaba.fastjson.JSONObject;
import com.razorpay.RazorpayException;
import com.startup.goHappy.integrations.service.RazorPayService;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@SpringBootTest
public class RazorPayControllerTest {

    private MockMvc mockMvc;

    @MockBean
    private RazorPayService razorPayService;

    @BeforeEach
    public void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(new RazorPayController()).build();
    }

    @Test
    public void testSetup() throws Exception {
        JSONObject params = new JSONObject();
        params.put("amount", "100");

        String orderId = "order_123456";

        when(razorPayService.pay(params.getString("amount"))).thenReturn(orderId);

        mockMvc.perform(MockMvcRequestBuilders.post("/razorPay/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(params.toJSONString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(orderId));
    }
}
