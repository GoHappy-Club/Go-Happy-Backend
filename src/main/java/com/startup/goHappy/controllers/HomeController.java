package com.startup.goHappy.controllers;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.List;

import javax.mail.MessagingException;

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
import com.startup.goHappy.integrations.service.EmailService;
import com.startup.goHappy.services.HomeService;

@RestController
@RequestMapping("home")
public class HomeController {
	
	@Autowired
	EmailService emailService;
	
		@GetMapping("getEvents")
		public String getEvents() throws Exception {
			emailService.sendSimpleMessage("","","");
			return "";
		}
}
