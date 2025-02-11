package com.startup.goHappy.entities.model;

import lombok.Data;

@Data
public class Reminder {
    @DocumentId
    private String id;
    private String title;
    private String description;
    private String phone;
    private String time; // in 24 hour format "22:10", separate hour & minutes by a colon
}
