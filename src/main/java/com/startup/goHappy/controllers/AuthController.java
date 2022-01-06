package com.startup.goHappy.controllers;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.startup.goHappy.entities.model.Event;
import com.startup.goHappy.entities.model.UserProfile;
import com.startup.goHappy.entities.repository.UserProfileRepository;

@RestController
@RequestMapping("auth")
public class AuthController {

	@Autowired
	UserProfileRepository userProfileService;
	@Autowired
	UserProfileController userProfileController;

	@PostMapping("register")
	public void register(@RequestBody JSONObject userProfile) {
		userProfileController.create(userProfile);
		return;
	}
	@PostMapping("login")
	public UserProfile login(@RequestBody JSONObject params) throws IOException, InterruptedException, ExecutionException {
		
		CollectionReference userProfiles = userProfileService.getCollectionReference();

		Query query = null;
//		if(StringUtils.isEmpty(params.getString("email"))) {
		query = userProfiles.whereEqualTo("phone", ""+params.getString("phone"));
//		}
//		else {
//			query = userProfiles.whereEqualTo("email", params.getString("email"));
//
//		}

		ApiFuture<QuerySnapshot> querySnapshot = query.get();
		UserProfile user = null;
		for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
			user = document.toObject(UserProfile.class);  
			break;
		}		
		if(user!=null) {
			return user;
		}
		else if(!StringUtils.isEmpty(params.getString("token"))) {
			register(params);
			ApiFuture<QuerySnapshot> querySnapshot1 = query.get();
			UserProfile user1 = null;
			for (DocumentSnapshot document : querySnapshot1.get().getDocuments()) {
				user1 = document.toObject(UserProfile.class);  
				break;
			}
			return user1;
		}
			
		return null;
	}
}
