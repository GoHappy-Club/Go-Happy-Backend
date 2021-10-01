package com.startup.goHappy.integrations.service;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONObject;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.startup.goHappy.integrations.model.ZoomMeetingObjectDTO;
import com.startup.goHappy.integrations.model.ZoomMeetingSettingsDTO;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;


@Service
public class RazorPayService {
	

	@Value("${razorPay.apiKey}")
    private String razorPayApiKey;
	@Value("${razorPay.apiSecret}")
    private String razorPayApiSecret;

	public String pay(String amount) throws RazorpayException {
		RazorpayClient razorpayClient = new RazorpayClient(razorPayApiKey, razorPayApiSecret);
		org.json.JSONObject options = new org.json.JSONObject();
		options.put("amount", amount);
		options.put("currency", "INR");
//		options.put("receipt", "txn_123456");
		Order order = razorpayClient.Orders.create(options);
//		Order order = razorpayClient.Orders.fetch("order_I0zXHKi7SP8ijQ");
		System.out.println(order);
		return order.get("id");
    }
	
    

}
