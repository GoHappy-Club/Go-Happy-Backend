package com.startup.goHappy.entities.model;



import java.util.*;

import org.springframework.context.annotation.Primary;


import lombok.Data;

@Data
public class Event {
	@DocumentId
	private String id;
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
	private Integer seatsLeft=30;
	private String type; //0:event,1:session
	private String lang; //0:event,1:session
	private String coverImage;
	private Boolean isScheduled=false;
	private Boolean isParent=false;
	private String parentId;
	private String cron;
	private String meetingLink;
	private String recordingLink;
	private String occurance;
	private List<String> participantList;

	private List<String> tambolaTickets;
	
	@Override
	public String toString() {
		return "Event [id=" + id + ", eventName=" + eventName + ", creator=" + creator + ", modifier=" + modifier
				+ ", lastUpdated=" + lastUpdated + ", startTime=" + startTime + ", endTime=" + endTime + ", eventDate="
				+ eventDate + ", expertName=" + expertName + ", category=" + category + ", description=" + description
				+ ", seatsLeft=" + seatsLeft + ", type=" + type + ", coverImage=" + coverImage + "]";
	}

	

	
	
	
	
}
