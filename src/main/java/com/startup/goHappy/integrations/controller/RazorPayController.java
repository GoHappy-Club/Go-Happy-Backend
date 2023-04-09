package com.startup.goHappy.integrations.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.razorpay.RazorpayException;
import com.startup.goHappy.entities.model.Event;
import com.startup.goHappy.integrations.model.ZoomMeetingObjectDTO;
import com.startup.goHappy.integrations.service.RazorPayService;
import com.startup.goHappy.integrations.service.ZoomService;

@RestController
@RequestMapping("razorPay")
public class RazorPayController {
	
	@Autowired
	RazorPayService razorPayService;
	
	@PostMapping("pay")
	public String setup(@RequestBody JSONObject params) throws IOException, RazorpayException {
		return razorPayService.pay(params.getString("amount"));
	}
}
