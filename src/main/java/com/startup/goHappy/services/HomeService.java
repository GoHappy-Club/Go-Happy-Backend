package com.startup.goHappy.services;

import java.util.List;

import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.startup.goHappy.entities.model.Event;

@Service
public class HomeService {
	

	public JSONObject getEvents() {
		JSONObject output = new JSONObject();
		return output;
	}
}
