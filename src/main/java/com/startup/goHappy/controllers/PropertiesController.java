package com.startup.goHappy.controllers;

import com.alibaba.fastjson.JSONObject;
import com.startup.goHappy.entities.model.Properties;
import com.startup.goHappy.entities.repository.PropertiesRepository;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("properties")
public class PropertiesController {
	
	@Autowired
	PropertiesRepository propertiesRepository;
	
		@GetMapping("list")
		public JSONObject getAllProperties() throws Exception {
			Iterable<Properties> properties = propertiesRepository.retrieveAll();
			List<Properties> result = IterableUtils.toList(properties);
			JSONObject output = new JSONObject();
			output.put("properties", result);
			return output;
		}
}
