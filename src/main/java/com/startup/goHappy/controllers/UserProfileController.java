package com.startup.goHappy.controllers;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;

import com.startup.goHappy.entities.model.Referral;
import com.startup.goHappy.entities.repository.ReferralRepository;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.v1.FirestoreClient;
import com.startup.goHappy.config.Firestore.FirestoreConfig;
import com.startup.goHappy.entities.model.Event;
import com.startup.goHappy.entities.model.UserProfile;
import com.startup.goHappy.entities.repository.EventRepository;
import com.startup.goHappy.entities.repository.UserProfileRepository;

@RestController
@RequestMapping("user")
public class UserProfileController {

	@Autowired
	UserProfileRepository userProfileService;

	@Autowired
	ReferralRepository referralService;
	
	
	@Autowired
	FirestoreConfig firestoreConfig;
	


	@PostMapping("create")
	public void create(@RequestBody JSONObject userProfile) {
		UserProfile up = new UserProfile();
		up.setId(UUID.randomUUID().toString());
		up.setAge(userProfile.getString("age"));
		up.setEmail(userProfile.getString("email"));
		up.setDateOfJoining(userProfile.getString("dateOfJoining"));
		up.setName(userProfile.getString("name"));
		up.setPhone(""+userProfile.getString("phone"));
		up.setProfileImage(userProfile.getString("profileImage"));
		up.setSessionsAttended("0");
		up.setPassword(userProfile.getString("password"));
		up.setGoogleSignIn(userProfile.getBoolean("googleSignIn"));
		String selfInviteId = RandomStringUtils.random(6,"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789");
		up.setSelfInviteCode(selfInviteId);
		userProfileService.save(up);
		return;
	}
	@PostMapping("delete")
	public void delete(@RequestBody JSONObject params) {
		String id = params.getString("id");
		userProfileService.delete(userProfileService.get(id).get());
		return;
	}
	@PostMapping("findAll")
	public JSONObject findAll() {
		Iterable<UserProfile> users = userProfileService.retrieveAll();
		List<UserProfile> result = IterableUtils.toList(users);
		JSONObject output = new JSONObject();
		output.put("users", result);
		return output;
	}
	@PostMapping("getUserByEmail")
	public JSONObject getUserByEmail(@RequestBody JSONObject params) throws IOException, InterruptedException, ExecutionException {
		CollectionReference userProfiles = userProfileService.getCollectionReference();

		Query query = userProfiles.whereEqualTo("email", params.getString("email"));

		ApiFuture<QuerySnapshot> querySnapshot = query.get();
		UserProfile user = null;
		for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
			user = document.toObject(UserProfile.class);  
			break;
		}		
//		List<UserProfile> result = IterableUtils.toList(user);
		JSONObject output = new JSONObject();
		output.put("user", user);
		return output;
	}
	
	@PostMapping("getUserByPhone")
	public JSONObject getUserByPhone(@RequestBody JSONObject params) throws IOException, InterruptedException, ExecutionException {
		CollectionReference userProfiles = userProfileService.getCollectionReference();

		Query query = userProfiles.whereEqualTo("phone", params.getString("phoneNumber"));

		ApiFuture<QuerySnapshot> querySnapshot = query.get();
		UserProfile user = null;
		for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
			user = document.toObject(UserProfile.class);  
			break;
		}		
		
		JSONObject output = new JSONObject();
		output.put("user", user);
		return output;
	}

//	@PostMapping("getTotalSessions")
//	public long totalSessions(@RequestBody JSONObject params) throws IOException {
//		NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
//				  .withQuery(QueryBuilders.termQuery("participantList.keyword",params.getString("email")))
//				  .build();
//		long sessionsCount = eventService.count(searchQuery);
//		return sessionsCount;
//
//	}
	
	@PostMapping("setMembership")
	public void setMembership(@RequestBody JSONObject params) throws IOException, InterruptedException, ExecutionException {
		CollectionReference userProfiles = userProfileService.getCollectionReference();

		Query query = userProfiles.whereEqualTo("email", params.getString("email"));

		ApiFuture<QuerySnapshot> querySnapshot = query.get();
		UserProfile user = null;
		for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
			user = document.toObject(UserProfile.class);  
			user.setMembership(params.getString("planName"));
			user.setLastPaymentDate(""+new Date().getTime());
			break;
		}		
		userProfileService.save(user);
	}
	
	@PostMapping("update")
	public UserProfile updateUser(@RequestBody JSONObject params) throws IOException, InterruptedException, ExecutionException {
		
		CollectionReference userProfiles = userProfileService.getCollectionReference();
		if(params.getString("phone").startsWith("+")){
			params.put("phone",params.getString("phone").substring(1));
		}
		Query query = userProfiles.whereEqualTo("phone", params.getString("phone"));

		ApiFuture<QuerySnapshot> querySnapshot = query.get();
		UserProfile user = null;
		for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
			user = document.toObject(UserProfile.class);  
			user.setName(params.getString("name"));
			if(!StringUtils.isEmpty(params.getString("email")))
				user.setEmail(params.getString("email"));
			if(!StringUtils.isEmpty(params.getString("phone")))
				user.setPhone(""+params.getLong("phone"));
			if(!StringUtils.isEmpty(params.getString("dob")))
				user.setDob(""+params.getString("dob"));
			if(!StringUtils.isEmpty(params.getString("age")))
				user.setAge(""+params.getString("age"));
			break;
		}
		userProfileService.save(user);
		return user;
	}
	@PostMapping("refer")
	public void refer(@RequestBody Referral referObject) throws IOException, InterruptedException, ExecutionException {
		CollectionReference referrals = referralService.getCollectionReference();
//		Referral refer = new Referral();
//		refer.setId(UUID.randomUUID().toString());
//		refer.setFrom(referObject.getFrom());
//		refer.setTo(referObject.getTo());
//		refer.setReferralId(referObject.getReferralId());

		Query query = referrals.whereEqualTo("to", referObject.getTo());

		ApiFuture<QuerySnapshot> querySnapshot = query.get();
		UserProfile user = null;
		if(querySnapshot.get().getDocuments().size()==0){
			referralService.save(referObject);
		}
	}
	@GetMapping("setReferIds")
	public void tempApi() throws ExecutionException, InterruptedException {
		CollectionReference userProfiles = userProfileService.getCollectionReference();
		Query query = userProfiles.whereNotEqualTo("phone","rakshit");
		ApiFuture<QuerySnapshot> querySnapshot = query.get();
		for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
			try {
				UserProfile user = document.toObject(UserProfile.class);
				if(user.getSelfInviteCode()!=null){
					continue;
				}
				String selfInviteId = RandomStringUtils.random(6,"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789");
				user.setSelfInviteCode(selfInviteId);
				userProfileService.save(user);
			}
			catch(Exception e){
				e.printStackTrace();
			}
//			break;
		}
	}
	@GetMapping("sessionAttended")
	public void sessionAttended(@RequestBody JSONObject userDetails) throws ExecutionException, InterruptedException {
		CollectionReference referrals = referralService.getCollectionReference();
//		Referral refer = new Referral();
//		refer.setId(UUID.randomUUID().toString());
//		refer.setFrom(referObject.getFrom());
//		refer.setTo(referObject.getTo());
//		refer.setReferralId(referObject.getReferralId());

		Query query = referrals.whereEqualTo("to", userDetails.getString("phone"));

		ApiFuture<QuerySnapshot> querySnapshot = query.get();
		Referral referral = null;
		if(querySnapshot.get().getDocuments().size()!=0){
			for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
				referral = document.toObject(Referral.class);
				referral.setHasAttendedSession(true);
				referralService.save(referral);
				break;
			}
		}
	}
}
