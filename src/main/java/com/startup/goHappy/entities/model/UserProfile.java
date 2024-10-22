package com.startup.goHappy.entities.model;

import lombok.Data;

@Data
public class UserProfile {
	@DocumentId
	private String id;
	private String name;
	private String dateOfJoining;
	private String dateOfJoiningDateObject;
	private String email;
	private String phone;
	private String sessionsAttended;
	private String password;
	private Boolean googleSignIn;
	private String profileImage;
	private String lastPaymentDate;
	private Integer lastPaymentAmount;
	private String selfInviteCode;
	private String age;
	private String source;
	private String city;
	private String emergencyContact;
	private String fcmToken;
}
