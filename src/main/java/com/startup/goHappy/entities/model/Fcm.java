package com.startup.goHappy.entities.model;

import lombok.Data;

import java.util.List;


@Data
public class Fcm {
    @DocumentId
    private String id;
    private String topicName;
    private List<String> fcmTokens;
}