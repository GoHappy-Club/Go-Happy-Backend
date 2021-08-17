package com.startup.goHappy.services;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.util.JSONPObject;

@Service
public class HomeService {

	public JSONObject getEvents() {
		JSONObject output = new JSONObject();
		output.put("title", "Health Awareness");
		return output;
	}
}
