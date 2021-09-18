package com.startup.goHappy.entities.model;



import java.util.*;

import org.springframework.context.annotation.Primary;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.Data;

@Data
@Document(indexName = "session", createIndex = true)
public class Event {
	@Id
	private String id;
	@Field(type = FieldType.Keyword)
	private String eventName;
	@Field(type = FieldType.Keyword)
	private String creator;
	@Field(type = FieldType.Keyword)
	private String modifier;
	@Field(type = FieldType.Keyword)
	private String lastUpdated;
	@Field(type = FieldType.Keyword)
	private String startTime;
	@Field(type = FieldType.Keyword)
	private String endTime;
	@Field(type = FieldType.Keyword)
	private String eventDate;
	@Field(type = FieldType.Keyword)
	private String expertName;
	@Field(type = FieldType.Binary)
	private String expertImage;
	@Field(type = FieldType.Keyword)
	private String category;
	@Field(type = FieldType.Keyword)
	private String description;
	@Field(type = FieldType.Integer)
	private Integer seatsLeft=30;
	@Field(type = FieldType.Keyword)
	private String type; //0:event,1:session
	@Field(type = FieldType.Keyword)
	private String lang; //0:event,1:session
	@Field(type = FieldType.Binary)
	private String coverImage;
	@Field(type = FieldType.Boolean)
	private Boolean isScheduled=false;
	@Field(type = FieldType.Boolean)
	private Boolean isParent=false;
	@Field(type = FieldType.Keyword)
	private String parentId;
	@Field(type = FieldType.Keyword)
	private String cron;
	@Field(type = FieldType.Keyword)
	private String meetingLink;
	@Field(type = FieldType.Nested)
	private List<String> participantList;
	@Override
	public String toString() {
		return "Event [id=" + id + ", eventName=" + eventName + ", creator=" + creator + ", modifier=" + modifier
				+ ", lastUpdated=" + lastUpdated + ", startTime=" + startTime + ", endTime=" + endTime + ", eventDate="
				+ eventDate + ", expertName=" + expertName + ", category=" + category + ", description=" + description
				+ ", seatsLeft=" + seatsLeft + ", type=" + type + ", coverImage=" + coverImage + "]";
	}

	

	
	
	
	
}
