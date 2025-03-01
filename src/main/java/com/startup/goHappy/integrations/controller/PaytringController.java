package com.startup.goHappy.integrations.controller;


import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.startup.goHappy.controllers.EventController;
import com.startup.goHappy.controllers.MembershipController;
import com.startup.goHappy.controllers.UserProfileController;
import com.startup.goHappy.entities.model.PaymentLog;
import com.startup.goHappy.entities.repository.PaymentLogRepository;
import com.startup.goHappy.enums.PaymentStatusEnum;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("paytring")
public class PaytringController {

    private RestTemplate restTemplate;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final String PAYTRING_BASE_URL = "https://api.paytring.com/api/v2";

    @Value("${paytring.key}")
    private String payKey;
    @Value("${paytring.pass}")
    private String payPass;
    /*
     * NOTES of PAYTRING
     * udf1 -> workshop ID
     * udf2 -> membership plan ID
     * udf3 -> voucher for session
     * udf4 -> coins
     * udf5 -> type of the payment, used later in verify function
     * */

    @Autowired
    PaymentLogRepository paymentsService;
    @Autowired
    private UserProfileController userProfileController;
    @Autowired
    private MembershipController membershipController;
    @Autowired
    private EventController eventController;

    public PaytringController() {
        this.restTemplate = new RestTemplate();
    }

    @ApiOperation(value = "Create order for the payment")
    @PostMapping("/createOrder")
    JSONObject createOrder(@RequestBody JSONObject params) throws IOException {
        /*
         * @params
         *       cname :- customer_name : String
         *       phone :- phone number  : String
         *       email :- email of user : String
         *       amount :- amount in RUPEES : String/Integer
         *       type :- Type of the payment/context : String
         *       Values for 'type' - contribution, workshop, coins, subscription
         * */
        HttpHeaders headers = new HttpHeaders();
        headers.add("accept", "application/json");
        headers.add("authorization", getBasicAuthToken());
        headers.add("content-type", "application/json");
        String receipt_id = UUID.randomUUID().toString().replace("-", "").substring(0, 29);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("key", payKey);
        requestBody.put("amount", Integer.parseInt(params.getString("amount")) * 100);
        requestBody.put("cname", params.getString("cname"));
        requestBody.put("phone", params.getString("phone"));
        requestBody.put("email", params.getString("email"));
        requestBody.put("receipt_id", receipt_id);
        requestBody.put("callback_url", "https://gohappyclub.in/free_sessions");
        requestBody.put("currency", "INR");
        Map<String, String> notes = new HashMap<>();
        if (!StringUtils.isEmpty(params.getString("workshopId"))) {
            notes.put("udf1", params.getString("workshopId"));
        }
        if (!StringUtils.isEmpty(params.getString("planId"))) {
            notes.put("udf2", params.getString("planId"));
        }
        if (!StringUtils.isEmpty(params.getString("voucherId"))) {
            notes.put("udf3", params.getString("voucherId"));
        }
        if (!StringUtils.isEmpty(params.getString("coinsToGive"))) {
            notes.put("udf4", params.getString("coinsToGive"));
        }
        notes.put("udf5", params.getString("type"));
        requestBody.put("notes", notes);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        JSONObject output = new JSONObject();

        String url = PAYTRING_BASE_URL + "/order/create";

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            JSONObject responseBody = objectMapper.readValue(response.getBody(), JSONObject.class);
            output.put("status", "success");
            output.put("order_id", responseBody.getString("order_id"));
            PaymentLog plog = new PaymentLog();
            plog.setId(receipt_id);
            plog.setAmount(Integer.parseInt(params.getString("amount")));
            plog.setPhone(params.getString("phone"));
            plog.setPaymentDate("" + new Date().getTime());
            plog.setType(params.getString("type"));
            plog.setOrderId(responseBody.getString("order_id"));
            paymentsService.save(plog);
            output.put("status", "success");
        } catch (Exception e) {
            output.put("status", "failed");
            e.printStackTrace();
        }
        return output;
    }

    @ApiOperation(value = "Verify whether the payment was successfull or not")
    @PostMapping("/verify")
    JSONObject verify(@RequestBody JSONObject params) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("accept", "application/json");
        headers.add("authorization", getBasicAuthToken());
        headers.add("content-type", "application/json");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("key", payKey);
        requestBody.put("id", params.getString("id"));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        String url = PAYTRING_BASE_URL + "/order/fetch";
        JSONObject output = new JSONObject();
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            JSONObject responseObject = objectMapper.readValue(response.getBody(), JSONObject.class);
            System.out.println(responseObject);
            JSONObject orderObject = responseObject.getJSONObject("order");
            CollectionReference paymentsRef = paymentsService.getCollectionReference();
            Query paymentDocQuery = paymentsRef.whereEqualTo("orderId", params.getString("id"));
            ApiFuture<QuerySnapshot> future = paymentDocQuery.get();
            PaymentLog plog = null;
            for (QueryDocumentSnapshot document : future.get().getDocuments()) {
                plog = document.toObject(PaymentLog.class);
                break;
            }
            assert plog != null;
            if(plog.getStatus()==PaymentStatusEnum.FAILED || plog.getStatus() == PaymentStatusEnum.SUCCESS){
                output.put("status", plog.getStatus().toString().toLowerCase());
                return output;
            }
            if (orderObject.getString("order_status").equals("success")) {
                plog.setStatus(PaymentStatusEnum.SUCCESS);
                paymentsService.save(plog);
                output.put("status", "success");
            } else if (orderObject.getString("order_status").equals("failed")) {
                plog.setStatus(PaymentStatusEnum.FAILED);
                output.put("status", "failed");
                paymentsService.save(plog);
                return output;
            } else {
                plog.setStatus(PaymentStatusEnum.PENDING);
                output.put("status", "pending");
                paymentsService.save(plog);
                return output;
            }
            if (orderObject.getString("order_status").equals("success")) {
                JSONObject notes = orderObject.getJSONObject("notes");
                String type = notes.getString("udf5");
                switch (type) {
                    case "workshop":
                        JSONObject bookEventParams = new JSONObject();
                        bookEventParams.put("phoneNumber", plog.getPhone());
                        bookEventParams.put("id", notes.getString("udf1"));
                        bookEventParams.put("tambolaTicket", "");
                        eventController.bookEvent(bookEventParams);
                        break;
                    case "contribution":
                        JSONObject setPaymentParams = new JSONObject();
                        setPaymentParams.put("amount", plog.getAmount());
                        setPaymentParams.put("phoneNumber", plog.getPhone());
                        userProfileController.setPaymentData(setPaymentParams);
                        break;
                    case "subscription":
                        membershipController.buySubscription(new JSONObject(), plog.getPhone(), String.valueOf(plog.getAmount()), notes.getString("udf2"));
                        break;
                    case "upgrade":
                        membershipController.upgradeSubscription(new JSONObject(), plog.getPhone(), String.valueOf(plog.getAmount()), notes.getString("udf2"));
                        break;
                    case "renewal":
                        membershipController.renewSubscription(new JSONObject(), plog.getPhone(), String.valueOf(plog.getAmount()), notes.getString("udf2"));
                        break;
                    case "topUp":
                        JSONObject topUpParams = new JSONObject();
                        topUpParams.put("id", plog.getId());
                        membershipController.topUpWallet(topUpParams, plog.getPhone(), String.valueOf(plog.getAmount()), notes.getString("udf4"));
                        break;
                }
            }
        } catch (Exception e) {
            output.put("status", "failed");
            e.printStackTrace();
        }
        return output;
    }

    @ApiOperation(value = "Webhook for Paytring")
    @PostMapping("/result")
    public void Success(@RequestBody JSONObject params) throws IOException {
        String orderId = params.getString("order_id");
        JSONObject verifyParams = new JSONObject();
        verifyParams.put("id", orderId);
        scheduleVerification(verifyParams);
    }

    @Async
    public CompletableFuture<Void> scheduleVerification(JSONObject verifyParams) {
        try {
            Thread.sleep(10000);
            verify(verifyParams);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private String getBasicAuthToken() {
        Base64.Encoder encoder = Base64.getEncoder();
        return "Basic " + encoder.encodeToString((payKey + ":" + payPass).getBytes());
    }
}
