package com.startup.goHappy.controllers;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
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
@RequestMapping("user")
public class UserProfileController {

	@Autowired
	UserProfileService userProfileService;
	
	@Autowired
	EventService eventService;

	@PostMapping("create")
	public void create(@RequestBody JSONObject userProfile) {
		UserProfile up = new UserProfile();
		up.setId(UUID.randomUUID().toString());
		up.setEmail(userProfile.getString("email"));
		up.setDateOfJoining(""+new Date().getTime());
		up.setName(userProfile.getString("name"));
		up.setPhone(userProfile.getString("phone"));
		up.setProfileImage(userProfile.getString("profileImage"));
		up.setSessionsAttended("0");
		up.setPassword(userProfile.getString("password"));
		up.setGoogleSignIn(userProfile.getBoolean("googleSignIn"));
		userProfileService.save(up);
		return;
	}
	@PostMapping("delete")
	public void delete(@RequestBody JSONObject params) {
		String id = params.getString("id");
		userProfileService.delete(id);
		return;
	}
	@PostMapping("findAll")
	public JSONObject findAll() {
		Iterable<UserProfile> users = userProfileService.findAll();
		List<UserProfile> result = IterableUtils.toList(users);
		JSONObject output = new JSONObject();
		output.put("users", result);
		return output;
	}
	@PostMapping("getUserByEmail")
	public JSONObject getUserByEmail(@RequestBody JSONObject params) throws IOException {
		QueryBuilder qb = QueryBuilders.matchQuery("email", params.getString("email"));
	
		Iterable<UserProfile> user = userProfileService.search(qb);
		List<UserProfile> result = IterableUtils.toList(user);
		JSONObject output = new JSONObject();
		output.put("user", result.get(0));
		return output;
	}
	
	@PostMapping("getUserByPhone")
	public JSONObject getUserByPhone(@RequestBody JSONObject params) throws IOException {
		QueryBuilder qb = QueryBuilders.matchQuery("phone", params.getString("phone"));
	
		Iterable<UserProfile> user = userProfileService.search(qb);
		List<UserProfile> result = IterableUtils.toList(user);
		JSONObject output = new JSONObject();
		output.put("user", result.get(0));
		return output;
	}
	
	@PostMapping("getTotalSessions")
	public long totalSessions(@RequestBody JSONObject params) throws IOException {
		NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
				  .withQuery(QueryBuilders.termQuery("participantList.keyword",params.getString("email")))
				  .build();
		long sessionsCount = eventService.count(searchQuery);
		return sessionsCount;
		
	}
	
	@PostMapping("setMembership")
	public void setMembership(@RequestBody JSONObject params) throws IOException {
		QueryBuilder qb = QueryBuilders.matchQuery("email", params.getString("email"));
		
		Iterable<UserProfile> user = userProfileService.search(qb);
		List<UserProfile> result = IterableUtils.toList(user);
		if(result.size()>0) {
			result.get(0).setMembership(params.getString("planName"));
			result.get(0).setLastPaymentDate(""+new Date().getTime());
		}
		userProfileService.save(result);
	}
}
