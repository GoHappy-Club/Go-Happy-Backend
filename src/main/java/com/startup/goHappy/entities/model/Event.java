package com.startup.goHappy.entities.model;

import org.springframework.context.annotation.Primary;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.Data;

@Data
@Document(indexName = "event", createIndex = true)
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
	@Field(type = FieldType.Keyword)
	private String category;
	@Field(type = FieldType.Keyword)
	private String description;
	@Field(type = FieldType.Keyword)
	private String type; //0:event,1:session
	@Field(type = FieldType.Binary)
	private String coverImage;
	@Override
	public String toString() {
		return "Event [id=" + id + ", eventName=" + eventName + ", creator=" + creator + ", modifier=" + modifier
				+ ", lastUpdated=" + lastUpdated + ", startTime=" + startTime + ", endTime=" + endTime + ", eventDate="
				+ eventDate + ", expertName=" + expertName + ", category=" + category + ", description=" + description
				+ ", type=" + type + ", coverImage=" + coverImage + "]";
	}

	
	
	
	
}
