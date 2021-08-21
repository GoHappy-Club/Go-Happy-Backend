package com.startup.goHappy.services;

import java.util.List;

import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.startup.goHappy.entities.model.Event;
import com.startup.goHappy.entities.service.EventService;

@Service
public class HomeService {
	
	@Autowired
	EventService eventService;

	public JSONObject getEvents() {
		Iterable<Event> data = eventService.findAll();
		List<Event> result = IterableUtils.toList(data);
		JSONObject output = new JSONObject();
		output.put("events", result);
		return output;
	}
}
