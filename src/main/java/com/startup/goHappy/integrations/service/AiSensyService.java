package com.startup.goHappy.integrations.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class AiSensyService {
    @Value("${aisensy.apikey}")
    private String apiKey;

    public String sendPaymentReminder() {
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, "{\n    \"apiKey\": apiKey,\n    \"campaignName\": \"First_campaign_for_api_testing\",\n    \"destination\": \"+16474654754\",\n    \"userName\": \"Nishtha Kapoor\",\n    \"templateParams\": [\n        \"50\"\n    ]\n}");
            Request request = new Request.Builder()
                    .url("https://backend.aisensy.com/campaign/t1/api")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

}

