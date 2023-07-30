package com.startup.goHappy.entities.model;

import lombok.Data;

import java.util.List;


@Data
public class Poster {
    @DocumentId
    private String id;
    private String imageUrl;
    private Boolean isExternal;
    private String url;
    private Boolean isActive;
    private Integer order;
}
