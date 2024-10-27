package com.startup.goHappy.integrations.controller;

import java.io.IOException;

import com.startup.goHappy.integrations.service.PhonePeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson.JSONObject;

@RestController
@RequestMapping("phonePe")
public class PhonePeController {
	
	@Autowired
	PhonePeService phonePeService;
	
	@PostMapping("generatePayload")
	public JSONObject setup(@RequestBody JSONObject params) throws IOException {
		return phonePeService.generatePayload(params.getString("phone"),params.getInteger("amount"),params.getString("paymentType"),params.getString("orderId"),params.getString("tambolaTicket"),params.getString("membershipId"),params.getString("coinsToGive"));
	}
}
