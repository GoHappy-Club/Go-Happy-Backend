package com.startup.goHappy.controllers;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.startup.goHappy.entities.model.Event;
import com.startup.goHappy.entities.model.Properties;
import com.startup.goHappy.entities.model.Trip;
import com.startup.goHappy.entities.repository.PropertiesRepository;
import com.startup.goHappy.entities.repository.TripRepository;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("trips")
public class TripsController {
	
	@Autowired
	TripRepository tripRepository;
	
	@GetMapping("list")
	public JSONObject list() throws Exception {
		Iterable<Trip> trips = tripRepository.retrieveAll();
		List<Trip> result = IterableUtils.toList(trips);
		JSONObject output = new JSONObject();
		output.put("trips", result);
		return output;
	}
	@GetMapping("upcoming")
	public JSONObject upcoming() throws Exception {
		CollectionReference tripsRef = tripRepository.getCollectionReference();
		Instant instance2 = java.time.Instant.now();
		ZonedDateTime zonedDateTime = java.time.ZonedDateTime
				.ofInstant(instance2,java.time.ZoneId.of("Asia/Kolkata"));
		Query queryNew = tripsRef.whereGreaterThanOrEqualTo("startTime", ""+zonedDateTime.toInstant().toEpochMilli());

		ApiFuture<QuerySnapshot> querySnapshotNew = queryNew.get();

		Set<Trip> trips = new HashSet<>();
		try {
			for (DocumentSnapshot document : querySnapshotNew.get().getDocuments()) {
				trips.add(document.toObject(Trip.class));
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		List<Trip> result = IterableUtils.toList(trips);
		JSONObject output = new JSONObject();
		output.put("trips", result);
		return output;
	}

	@GetMapping("past")
	public JSONObject past() throws Exception {
		CollectionReference tripsRef = tripRepository.getCollectionReference();
		Instant instance2 = java.time.Instant.now();
		ZonedDateTime zonedDateTime = java.time.ZonedDateTime
				.ofInstant(instance2, java.time.ZoneId.of("Asia/Kolkata"));
		Query queryNew = tripsRef.whereLessThan("startTime", "" + zonedDateTime.toInstant().toEpochMilli());

		ApiFuture<QuerySnapshot> querySnapshotNew = queryNew.get();

		Set<Trip> trips = new HashSet<>();
		try {
			for (DocumentSnapshot document : querySnapshotNew.get().getDocuments()) {
				trips.add(document.toObject(Trip.class));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<Trip> result = IterableUtils.toList(trips);
		JSONObject output = new JSONObject();
		output.put("trips", result);
		return output;
	}

	@PostMapping("add")
	public void add(@RequestBody JSONObject tripObject) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Trip trip = objectMapper.readValue(tripObject.toJSONString(), Trip.class);
		tripRepository.save(trip);

		return ;
	}
}
