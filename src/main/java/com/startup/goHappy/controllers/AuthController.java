package com.startup.goHappy.controllers;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import com.startup.goHappy.entities.model.Referral;
import io.swagger.annotations.ApiOperation;
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

	@ApiOperation(value = "To register the user on the platform")
	@PostMapping("register")
	public void register(@RequestBody JSONObject userProfile, @RequestBody JSONObject referDetails) throws IOException, ExecutionException, InterruptedException {
		userProfileController.create(userProfile);
		Referral referObject = new Referral();
		referObject.setId(UUID.randomUUID().toString());
		CollectionReference userProfiles = userProfileService.getCollectionReference();
		Query query = null;
//		if(StringUtils.isEmpty(params.getString("email"))) {
		query = userProfiles.whereEqualTo("selfInviteCode", ""+referDetails.getString("referralId"));
		ApiFuture<QuerySnapshot> querySnapshot = query.get();
		UserProfile refereeUser = null;
		for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
			refereeUser = document.toObject(UserProfile.class);
			referObject.setFrom(refereeUser.getPhone());
			referObject.setReferralId(referDetails.getString("referralId"));

			referObject.setTo(userProfile.getString("phone"));
			referObject.setTime(""+new Date().getTime());
			referObject.setHasAttendedSession(false);

			userProfileController.refer(referObject);

			break;
		}
		return;
	}

	@ApiOperation(value = "To login the user and generate token")
	@PostMapping("login")
	public UserProfile login(@RequestBody JSONObject params) throws IOException, InterruptedException, ExecutionException {
		
		CollectionReference userProfiles = userProfileService.getCollectionReference();

		Query query = null;
		query = userProfiles.whereEqualTo("phone", ""+params.getString("phone"));

		ApiFuture<QuerySnapshot> querySnapshot = query.get();
		UserProfile user = null;
		for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
			user = document.toObject(UserProfile.class);  
			break;
		}		
		if(user!=null) {
			if(!StringUtils.isEmpty(params.getString("fcmToken"))) {
				user.setFcmToken(params.getString("fcmToken"));
				userProfileService.save(user);
			}
			return user;
		}
		else if(!StringUtils.isEmpty(params.getString("token"))) {
			Instant instance = java.time.Instant.ofEpochMilli(new Date().getTime());
			ZonedDateTime zonedDateTime = java.time.ZonedDateTime
			                            .ofInstant(instance,java.time.ZoneId.of("Asia/Kolkata"));
			params.put("dateOfJoining", ""+zonedDateTime.toInstant().toEpochMilli());
			params.put("dateOfJoiningDateObject", ""+zonedDateTime.toLocalDateTime().toString());
			JSONObject referDetails = new JSONObject();
			referDetails.put("referralId",params.getString("referralId"));
			params.remove("referralId");
			register(params,referDetails);
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
