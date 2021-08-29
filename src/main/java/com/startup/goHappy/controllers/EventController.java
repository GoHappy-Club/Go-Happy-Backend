package com.startup.goHappy.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.startup.goHappy.entities.model.Event;
import com.startup.goHappy.entities.service.EventService;

@RestController
@RequestMapping("event")
public class EventController {

	@Autowired
	EventService eventService;

	@PostMapping("create")
	public void createEvent(@RequestBody JSONObject event) {
		Event ev = new Event();
		ev.setId(UUID.randomUUID().toString());
		ev.setCategory(event.getString("category"));
		ev.setCreator(event.getString("creator"));
		ev.setDescription(event.getString("description"));
		ev.setEndTime(event.getString("endTime"));
		ev.setEventDate(event.getString("eventDate"));
		ev.setEventName(event.getString("eventName"));
		ev.setExpertName(event.getString("expertName"));
		ev.setStartTime(event.getString("startTime"));
		ev.setType(StringUtils.isEmpty(event.getString("type"))?"0":event.getString("type"));
		ev.setSeatsLeft(event.getInteger("seatsLeft"));
		eventService.save(ev);
		return;
	}
	@PostMapping("delete")
	public void deleteEvent(@RequestBody JSONObject params) {
		String id = params.getString("id");
		eventService.delete(id);
		return;
	}
	@PostMapping("findAll")
	public JSONObject findAll() {
		Iterable<Event> events = eventService.findAll();
		List<Event> result = IterableUtils.toList(events);
		JSONObject output = new JSONObject();
		output.put("events", result);
		return output;
	}
	@PostMapping("getEventsByDate")
	public JSONObject getEventsByDate(@RequestBody JSONObject params) throws IOException {
		QueryBuilder qb = QueryBuilders.matchQuery("eventDate", params.getString("date"));
		Iterable<Event> events = eventService.search(qb);
		List<Event> result = IterableUtils.toList(events);
		Collections.sort(result,(a, b) -> a.getStartTime().compareTo(b.getStartTime()));
		JSONObject output = new JSONObject();
		output.put("events", result);
		return output;
	}
	@PostMapping("bookEvent")
	public String bookEvent(@RequestBody JSONObject params) throws IOException {
		Event event = eventService.findById(params.getString("id"));
		if(event.getSeatsLeft()<=0) {
			return "FAILED:FULL";
		}
		event.setSeatsLeft(event.getSeatsLeft()-1);
		List<String> participants = event.getParticipantsList();
		if(participants==null) {
			participants = new ArrayList<String>();
		}
		participants.add(params.getString("email"));
		event.setParticipantsList(participants);
		eventService.save(event);
		return "SUCCESS";
	}
	@PostMapping("mySessions")
	public JSONObject mySessions(@RequestBody JSONObject params) throws IOException {
		QueryBuilder upcomingQb = QueryBuilders.boolQuery().must(QueryBuilders.termQuery("participantsList.keyword",params.getString("email")))
				.must(QueryBuilders.rangeQuery("startTime").gte(""+new Date().getTime()));
		Iterable<Event> upcomingEvents = eventService.search(upcomingQb);
		List<Event> upresult = IterableUtils.toList(upcomingEvents);
		Collections.sort(upresult,(a, b) -> a.getStartTime().compareTo(b.getStartTime()));
		
		QueryBuilder ongoingQb = QueryBuilders.boolQuery().must(QueryBuilders.termQuery("participantsList.keyword",params.getString("email")))
				.must(QueryBuilders.rangeQuery("startTime").lte(""+new Date().getTime()))
				.must(QueryBuilders.rangeQuery("endTime").gte(""+new Date().getTime()));
		Iterable<Event> ongoingEvents = eventService.search(ongoingQb);
		List<Event> ogresult = IterableUtils.toList(ongoingEvents);
		Collections.sort(ogresult,(a, b) -> a.getStartTime().compareTo(b.getStartTime()));
		

		QueryBuilder expiredQb = QueryBuilders.boolQuery().must(QueryBuilders.termQuery("participantsList.keyword",params.getString("email")))
		.must(QueryBuilders.rangeQuery("endTime").lte(""+new Date().getTime()));
		Iterable<Event> expiredEvents = eventService.search(expiredQb);
		List<Event> expresult = IterableUtils.toList(expiredEvents);
		Collections.sort(expresult,(a, b) -> a.getStartTime().compareTo(b.getStartTime()));

		
		JSONObject output = new JSONObject();
		output.put("upcomingEvents", upresult);
		output.put("ongoingEvents", ogresult);
		output.put("expiredEvents", expresult);
		return output;
	}
}  

