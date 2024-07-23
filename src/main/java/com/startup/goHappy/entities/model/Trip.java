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
	private String inquireNowLink = "https://wa.me/7888384477?text=Hi%20GoHappy%20Club%20Team%2C%20%20I%20am%20interested%20in%20%24%7Btrip%7D%20trip.%20Please%20share%20more%20details.";
	private String memoryImage;
	private String memoryVideoUrl;

}
