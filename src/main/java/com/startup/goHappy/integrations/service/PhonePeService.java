package com.startup.goHappy.integrations.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class PhonePeService {
    @Value("${phonePe.key}")
    private String key;
    @Value("${phonePe.keyIndex}")
    private Integer keyIndex;
    @Value("${phonePe.merchantId}")
    private String merchantId;
    @Value("${phonePe.apiEndPoint}")
    private String apiEndPoint;
    @Value("${base.url}")
    private String baseUrl;

	public JSONObject generatePayload(String phone,Integer amount,String paymentType,String orderId,String tambolaTicket) throws JsonProcessingException, UnsupportedEncodingException {
		String merchantTransactionId = UUID.randomUUID().toString().replace("-", "");
		JSONObject requestBody = new JSONObject();
		requestBody.put("merchantId",merchantId);
		requestBody.put("merchantTransactionId",merchantTransactionId);
		requestBody.put("mobileNumber",phone);
		requestBody.put("amount",amount);
        String encodedPhoneNumber = phone != null ? URLEncoder.encode(phone, "UTF-8") : "";
        String encodedOrderId = orderId != null ? URLEncoder.encode(orderId, "UTF-8") : "";
        String encodedTambolaTicket = tambolaTicket != null ? URLEncoder.encode(tambolaTicket, "UTF-8") : "";
		if ("contribution".equals(paymentType)) {
			requestBody.put("callbackUrl",baseUrl+"/user/setPaymentDataContribution?phoneNumber="+encodedPhoneNumber);
            requestBody.put("redirectUrl","https://www.gohappyclub.in/contribute");
		}else if("workshop".equals(paymentType)){
			requestBody.put("callbackUrl",baseUrl+"/user/setPaymentDataWorkshop?phoneNumber="+encodedPhoneNumber+"&orderId="+encodedOrderId+"&tambolaTicket="+encodedTambolaTicket);
            requestBody.put("redirectUrl","https://www.gohappyclub.in/free_sessions");
		}
		requestBody.put("merchantUserId",phone);
		JSONObject paymentInstrument = new JSONObject();
		paymentInstrument.put("type","PAY_PAGE");
		requestBody.put("paymentInstrument",paymentInstrument);
		ObjectMapper objectMapper = new ObjectMapper();
		byte[] bytes = objectMapper.writeValueAsBytes(requestBody);
		String base64Encoded = Base64.getEncoder().encodeToString(bytes);
		String checksum = generateChecksum(base64Encoded);
		JSONObject payload = new JSONObject();
		payload.put("requestBody",base64Encoded);
		payload.put("checksum",checksum);
		payload.put("merchantTransactionId",merchantTransactionId);
		return payload;
    }

    private String generateChecksum(String base64Body) {
        return calculateSha256(base64Body + apiEndPoint + key) + "###" + keyIndex;
    }

    private static String calculateSha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            // Convert the byte array to a hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
