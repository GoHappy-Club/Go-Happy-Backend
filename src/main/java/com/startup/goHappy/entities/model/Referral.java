package com.startup.goHappy.entities.model;

import kotlin.jvm.Transient;
import lombok.Data;


@Data
public class Referral {
	@DocumentId
	private String id;
	private String from; //from phone
	private String referralId; //from referralId
	private String to;  // to phone
	private String time;
	private boolean hasAttendedSession;
	@Transient
	private String toName;
	@Transient
	private String toProfileImage;


}
