package com.startup.goHappy.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.startup.goHappy.entities.model.Event;
import com.startup.goHappy.entities.repository.EventRepository;
import com.startup.goHappy.services.HomeService;

@RestController
@RequestMapping("home")
public class HomeController {
	
	@Autowired
	HomeService homeService;
	@Autowired
	EventRepository erepo;
	
	
		@GetMapping("getEvents")
		public List<Event> getEvents() {
			return erepo.retrieveAll();
		}
}
