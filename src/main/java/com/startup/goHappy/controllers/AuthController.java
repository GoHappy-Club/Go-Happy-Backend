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
import com.startup.goHappy.entities.model.UserProfile;
import com.startup.goHappy.entities.service.EventService;
import com.startup.goHappy.entities.service.UserProfileService;

@RestController
@RequestMapping("auth")
public class AuthController {

	@Autowired
	UserProfileService userProfileService;
	@Autowired
	UserProfileController userProfileController;

	@PostMapping("register")
	public void register(@RequestBody JSONObject userProfile) {
		userProfileController.create(userProfile);
		return;
	}
	@PostMapping("login")
	public UserProfile login(@RequestBody JSONObject params) throws IOException {
		BoolQueryBuilder qb = new BoolQueryBuilder();
		if(StringUtils.isEmpty(params.getString("email"))) {
			qb.must(QueryBuilders.matchQuery("phone", params.getString("phone")));
		}
		else
			qb.must(QueryBuilders.matchQuery("email", params.getString("email")));
		Iterable<UserProfile> user = userProfileService.search(qb);
		List<UserProfile> result = IterableUtils.toList(user);
		if(result.size()==1) {
			return result.get(0);
		}
		else if(StringUtils.isEmpty(params.getString("password")) && 
				!StringUtils.isEmpty(params.getString("token"))) {
			register(params);
			Iterable<UserProfile> user1 = userProfileService.search(qb);
			List<UserProfile> result1 = IterableUtils.toList(user1);
			if(result1.size()==1) {
				return result1.get(0);
			}
		}
			
		return null;
	}
}
