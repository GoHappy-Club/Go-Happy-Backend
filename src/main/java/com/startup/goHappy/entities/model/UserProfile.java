package com.startup.goHappy.entities.model;

import org.springframework.context.annotation.Primary;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.Data;

@Data
@Document(indexName = "userprofile", createIndex = true)
public class UserProfile {
	@Id
	private String id;
	@Field(type = FieldType.Keyword)
	private String name;
	@Field(type = FieldType.Keyword)
	private String dateOfJoining;
	@Field(type = FieldType.Keyword)
	private String email;
	@Field(type = FieldType.Keyword)
	private String phone;
	@Field(type = FieldType.Binary)
	private String profileImage;
	@Override
	public String toString() {
		return "UserProfile [id=" + id + ", name=" + name + ", dateOfJoining=" + dateOfJoining + ", email=" + email
				+ ", phone=" + phone + ", profileImage=" + profileImage + "]";
	}	
}
