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
	@Field(type = FieldType.Keyword)
	private String sessionsAttended;
	@Field(type = FieldType.Keyword)
	private String password;
	@Field(type = FieldType.Boolean)
	private Boolean googleSignIn;
	@Field(type = FieldType.Binary)
	private String profileImage;
	@Field(type = FieldType.Keyword)
	private String membership;
	@Field(type = FieldType.Keyword)
	private String lastPaymentDate;

}
