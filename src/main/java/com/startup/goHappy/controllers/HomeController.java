package com.startup.goHappy.controllers;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;

import javax.mail.MessagingException;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.startup.goHappy.entities.model.Poster;
import com.startup.goHappy.entities.model.Trip;
import com.startup.goHappy.entities.repository.PosterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.startup.goHappy.entities.model.Event;
import com.startup.goHappy.entities.repository.EventRepository;
//import com.startup.goHappy.integrations.service.EmailService;
//import com.startup.goHappy.services.HomeService;



@RestController
@RequestMapping("/home")
public class HomeController {

	@Autowired
	EventRepository eventService;

	@Autowired
	PosterRepository posterService;

	static class TrendingEventClass<K, V> {
		private K key;
		private K normKey;
		private V value;

		public TrendingEventClass(K key, V value) {
			this.key = key;
			this.value = value;
		}

		public K getKey() {
			return key;
		}

		public K getNormKey() {
			return normKey;
		}

		public V getValue() {
			return value;
		}
		public void setKey(K key) {
			this.key = key;
		}
		public void setNormKey(K key) {
			this.normKey = key;
		}
	}
	
	@GetMapping("getEvents")
	public String getEvents() throws Exception {
		//emailService.sendSimpleMessage("","","");
		return "";
	}

	@GetMapping("/overview")
	public JSONObject overview() throws Exception {
		JSONObject content = new JSONObject();
		content.put("trendingSessions",trendingSessions());
		content.put("upcomingWorkshops",upcomingWorkshops());
		content.put("posters",posters());
		return content;
	}
	public List<TrendingEventClass<Double,Event>> trendingSessions() throws ExecutionException, InterruptedException {
		JSONObject sessions = new JSONObject();
		List<TrendingEventClass<Double,Event>> upcomingSessions = new ArrayList<>();
		CollectionReference eventsRef = eventService.getCollectionReference();

		Instant instance = java.time.Instant.ofEpochMilli(new Date().getTime());
		ZonedDateTime zonedDateTime = java.time.ZonedDateTime
				.ofInstant(instance,java.time.ZoneId.of("Asia/Kolkata"));
		ZonedDateTime zonedDateTimeAfter14 = java.time.ZonedDateTime
				.ofInstant(instance,java.time.ZoneId.of("Asia/Kolkata")).plusDays(14);

		Query query = eventsRef.whereLessThan("endTime", ""+zonedDateTimeAfter14.toInstant().toEpochMilli()).whereGreaterThan("endTime",""+zonedDateTime.toInstant().toEpochMilli());
//				.whereNotEqualTo("type", "workshop");
		ApiFuture<QuerySnapshot> querySnapshot = query.get();
		for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
			Event ev = document.toObject(Event.class);
			Integer seatsLeft = ev.getSeatsLeft();

			Integer totalSeats = 0;
			if(ev.getParticipantList()!=null){
				totalSeats = seatsLeft + ev.getParticipantList().size();
			}
			else{
				totalSeats = seatsLeft;
			}
			double fillRate = (double)(totalSeats - seatsLeft) / totalSeats;
			Double trendingScore = fillRate * Math.log(totalSeats);
			DecimalFormat decimalFormat = new DecimalFormat("##.##");
			fillRate = Double.parseDouble(decimalFormat.format(fillRate));
			trendingScore = Double.parseDouble(decimalFormat.format(trendingScore));

			upcomingSessions.add(new TrendingEventClass<Double,Event>(trendingScore,ev));
		}


		normalizeTrendingScores(upcomingSessions);

		// Set the threshold for trending sessions
		double threshold = 0.7;

		// Filter the trending sessions based on the threshold
		List<TrendingEventClass<Double, Event>> filteredSessions = new ArrayList<>();
		for (TrendingEventClass<Double, Event> session : upcomingSessions) {
			if (session.getNormKey() >= threshold) {
				filteredSessions.add(session);
			}
		}

		// Sort the filtered sessions by the normalized trending scores
		Collections.sort(filteredSessions, new Comparator<TrendingEventClass<Double, Event>>() {
			@Override
			public int compare(TrendingEventClass<Double, Event> o1, TrendingEventClass<Double, Event> o2) {
				return Double.compare(o2.getKey(), o1.getKey());
			}
		});

		return filteredSessions;
	}
	// Normalize the trending scores of sessions
	static void normalizeTrendingScores(List<TrendingEventClass<Double, Event>> sessions) {
		// Find the maximum trending score
		double maxTrendingScore = sessions.stream()
				.mapToDouble(TrendingEventClass::getKey)
				.max()
				.orElse(0.0);

		// Normalize the trending scores
		for (TrendingEventClass<Double, Event> session : sessions) {
			DecimalFormat decimalFormat = new DecimalFormat("##.##");
			double normalizedScore = session.getKey() / maxTrendingScore;
			session.setNormKey(Double.parseDouble(decimalFormat.format(normalizedScore)));
		}
	}
	public List<Event> upcomingWorkshops() throws ExecutionException, InterruptedException {
		List<Event> workshops = new ArrayList<>();

		CollectionReference eventsRef = eventService.getCollectionReference();

		Instant instance = java.time.Instant.ofEpochMilli(new Date().getTime());
		ZonedDateTime zonedDateTime = java.time.ZonedDateTime
				.ofInstant(instance,java.time.ZoneId.of("Asia/Kolkata"));

		Query query = eventsRef.whereGreaterThanOrEqualTo("endTime", ""+zonedDateTime.toInstant().toEpochMilli()).whereEqualTo("type", "workshop");
		ApiFuture<QuerySnapshot> querySnapshot = query.get();
		for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
			workshops.add(document.toObject(Event.class));
		}
		workshops.sort(Comparator.comparing(Event::getStartTime));
		return workshops;
	}

	public List<Poster> posters() throws ExecutionException, InterruptedException {
		List<Poster> posters = new ArrayList<>();

		CollectionReference postersRef = posterService.getCollectionReference();

		ApiFuture<QuerySnapshot> querySnapshot = postersRef.get();
		for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
			posters.add(document.toObject(Poster.class));
		}
		return posters;
	}
}
