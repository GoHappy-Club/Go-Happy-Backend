package com.startup.goHappy.entities.model;



import java.util.*;

import com.google.firebase.database.utilities.Pair;
import org.springframework.context.annotation.Primary;


import lombok.Data;

@Data
public class Event {
	@DocumentId
	private String id;
	private String costType; //"free" || "paid"
	private int cost;
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
	private String type; //"session" || "workshop"
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
	private List<String> participantList;
	private List<Integer> tambolaNumberCaller;
	private Map<String,Integer> liveTambola;

	private List<String> tambolaTickets;

	private String sameDayEventId;

//	private Map<Pair<String,String>,String> tambolaTicketsMapping;
	
	@Override
	public String toString() {
		return "Event [id=" + id + ", eventName=" + eventName + ", creator=" + creator + ", modifier=" + modifier
				+ ", lastUpdated=" + lastUpdated + ", startTime=" + startTime + ", endTime=" + endTime + ", eventDate="
				+ eventDate + ", expertName=" + expertName + ", category=" + category + ", description=" + description
				+ ", seatsLeft=" + seatsLeft + ", type=" + type + ", coverImage=" + coverImage + ", tambolaNumberCaller="+tambolaNumberCaller  + "]";
	}

	

	
	
	
	
}
