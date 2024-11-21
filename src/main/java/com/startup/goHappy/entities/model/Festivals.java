package com.startup.goHappy.entities.model;

import lombok.Data;

@Data
public class Festivals {
    @DocumentId
    private String id;
    private String name;
    private String asset;
    private boolean active;
    private String title;
    private String isoDate;
    private String message;
}
