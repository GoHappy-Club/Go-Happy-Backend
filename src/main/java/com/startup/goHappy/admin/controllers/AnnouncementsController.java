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

    @Autowired
    CloudNotification cloudNotification;

    public AnnouncementsController(CloudNotification cloudNotification) {
        this.cloudNotification = cloudNotification;
    }

    @ApiOperation(value = "To anounce trips via a push notification")
    @PostMapping("/trips")
    public void tripNotify(@RequestBody JSONObject params) throws IOException, ExecutionException, InterruptedException, FirebaseMessagingException {
        cloudNotification.sendTripUpdate(params.getString("title"), params.getString("body"));
    }

    @ApiOperation(value = "Promote a session via a push notification with embedded deep link")
    @PostMapping("/session")
    public void sessionNotify(@RequestBody JSONObject params) throws IOException, ExecutionException, InterruptedException, FirebaseMessagingException {
        cloudNotification.promoteSession(params.getString("title"), params.getString("body"), params.getString("id"));
    }
}
