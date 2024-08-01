package com.startup.goHappy.entities.model;


import lombok.Data;
import java.util.List;

@Data
public class Trip {
	@DocumentId
	private String id;
	private int cost;
	private String startTime;
	private String endTime;
	private List<String> coverImages;
	private List<String> participantList;
	private Integer seatsLeft=30;
	private String location;
	private String title;
	private List<String> cities;
	private String aboutTheLocation;
	private String highlights;
	private String itinerary;
	private String inclusion;
	private String exclusion;
	private String paymentPlan;
	private String docsRequired;
	private String cancellationPolicy = "default policy";
	private String termsAndConditions = "default";
	private String inquireNowLink = "https://wa.me/7888384477?text=Hi GoHappy Club Team,  I am interested in ${trip} trip. Please share more details.";
	private String memoryImage;
	private String memoryVideoUrl;

}
