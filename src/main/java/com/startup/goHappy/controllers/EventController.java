package com.startup.goHappy.controllers;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
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
		ev.setSeatsLeft(event.getString("seatsLeft"));
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
		JSONObject output = new JSONObject();
		output.put("events", result);
		return output;
	}
}
