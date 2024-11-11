package com.startup.goHappy.entities.model;

import lombok.Data;

@Data
public class Templates {
    @DocumentId
    private String id;
    private String templateName;
    private String costType = "free"; //"free" || "paid"
    private int cost=0;
    private String eventName;
    private String creator;
    private String modifier;
    private String lastUpdated;
    private String startTime;
    private String endTime;
    private String eventDate;
    private String expertName;
    private String expertImage;
    private String category;
    private String description;
    private String beautifulDescription;
    private String shareMessage;
    private Integer seatsLeft=30;
    private String type = "session"; //"session" || "workshop"
    private String lang; //0:event,1:session
    private String coverImage;
    private Boolean isScheduled=false;
    private Boolean isParent=false;
    private String parentId;
    private String cron;
    private String meetingLink;
    private String meetingId;
    private String recordingLink;
    private String occurance;
    private String sameDayEventId;
}
