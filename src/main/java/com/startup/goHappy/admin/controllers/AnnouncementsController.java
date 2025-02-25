package com.startup.goHappy.admin.controllers;


import com.alibaba.fastjson.JSONObject;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.startup.goHappy.notifications.CloudNotification;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/anouncements")
public class AnnouncementsController {

    private final String PREFIX_URL = "https://gohappyclub.in";

    @Autowired
    CloudNotification cloudNotification;

    public AnnouncementsController(CloudNotification cloudNotification) {
        this.cloudNotification = cloudNotification;
    }

    @ApiOperation(value = "To anounce trips via a push notification")
    @PostMapping("/trips")
    public void tripNotify(@RequestBody JSONObject params) throws IOException, ExecutionException, InterruptedException, FirebaseMessagingException {
        String deepLink = PREFIX_URL+"/trips";
        cloudNotification.sendAnnouncement(params.getString("title"), params.getString("body"),deepLink);
    }

    @ApiOperation(value = "Promote a session via a push notification with embedded deep link")
    @PostMapping("/session")
    public void sessionNotify(@RequestBody JSONObject params) throws IOException, ExecutionException, InterruptedException, FirebaseMessagingException {
        String deepLink = PREFIX_URL+"/session_details/"+params.getString("session_id");
        cloudNotification.sendAnnouncement(params.getString("title"), params.getString("body"), deepLink);
    }
}
