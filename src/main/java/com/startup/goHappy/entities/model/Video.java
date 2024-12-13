package com.startup.goHappy.entities.model;

import lombok.Data;

@Data
public class Video {
    @DocumentId
    private String id;
    private String category;
    private String thumbnail;
    private String title;
    private String videoUrl;
    private String playlistLink;
    private Double random;
}
