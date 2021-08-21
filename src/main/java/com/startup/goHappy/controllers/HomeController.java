package com.startup.goHappy.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.startup.goHappy.services.HomeService;

@RestController
@RequestMapping("home")
public class HomeController {
	
	@Autowired
	HomeService homeService;
	
	
	
		@PostMapping("getEvents")
		public JSONObject getEvents() {
			return homeService.getEvents();
		}
}
