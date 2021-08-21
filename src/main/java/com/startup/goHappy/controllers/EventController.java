package com.startup.goHappy.controllers;

import java.util.UUID;

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
		eventService.findAll();
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
		eventService.save(ev);
		return;
	}
}
