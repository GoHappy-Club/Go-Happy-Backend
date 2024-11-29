package com.startup.goHappy.entities.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Ratings {
    @DocumentId
    private String id;
    private String subCategory;
    private Integer rating;
    private String phone;
    private String reason;
    private String eventId;
}
