package com.startup.goHappy.controllers;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import com.google.cloud.firestore.*;
import com.startup.goHappy.entities.model.PaymentLog;
import com.startup.goHappy.entities.model.Referral;
import com.startup.goHappy.entities.repository.PaymentLogRepository;
import com.startup.goHappy.entities.repository.ReferralRepository;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.TestOnly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.google.api.core.ApiFuture;
import com.startup.goHappy.config.Firestore.FirestoreConfig;
import com.startup.goHappy.entities.model.UserProfile;
import com.startup.goHappy.entities.repository.UserProfileRepository;

@RestController
@RequestMapping("user")
public class UserProfileController {

	@Autowired
	UserProfileRepository userProfileService;

	@Autowired
	PaymentLogRepository paymentLogService;

	@Autowired
	ReferralRepository referralService;
	
	
	@Autowired
	FirestoreConfig firestoreConfig;


//	@Autowired
//	UserProfileManager userProfileManager;

	public UserProfileController() {

	}

	@TestOnly
	public UserProfileController(UserProfileRepository userProfileService) {
		this.userProfileService = userProfileService;
	}

	@PostMapping("create")
	public void create(@RequestBody JSONObject userProfile) {
		UserProfile up = new UserProfile();
		up.setId(UUID.randomUUID().toString());
		up.setAge(userProfile.getString("age"));
		up.setEmail(userProfile.getString("email"));
		up.setDateOfJoining(userProfile.getString("dateOfJoining"));
		up.setDateOfJoiningDateObject(userProfile.getString("dateOfJoiningDateObject"));
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
	
	@PostMapping("setPaymentData")
	public void setPaymentData(@RequestBody JSONObject params) throws IOException, InterruptedException, ExecutionException {
		CollectionReference userProfiles = userProfileService.getCollectionReference();

		Query profileQuery = userProfiles.whereEqualTo("phone", params.getString("phoneNumber"));

		ApiFuture<QuerySnapshot> querySnapshot1 = profileQuery.get();
		UserProfile user = null;
		for (DocumentSnapshot document : querySnapshot1.get().getDocuments()) {
			user = document.toObject(UserProfile.class);  
			user.setLastPaymentAmount(Integer.parseInt(params.getString("amount")));
			user.setLastPaymentDate(""+new Date().getTime());

			PaymentLog log = new PaymentLog();
			log.setPaymentDate(user.getLastPaymentDate());
			log.setPhone(user.getPhone());
			log.setId(UUID.randomUUID().toString());
			log.setAmount(user.getLastPaymentAmount());
			log.setType("contribution");
			paymentLogService.save(log);


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
//			if(!StringUtils.isEmpty(params.getString("dob")))
//				user.setDob(""+params.getString("dob"));
			if(!StringUtils.isEmpty(params.getString("age")))
				user.setAge(""+params.getString("age"));
			break;
		}
		userProfileService.save(user);
		return user;
	}

	@PostMapping("updateProfileImage")
	public UserProfile updateProfileImage(@RequestBody JSONObject params) throws IOException, InterruptedException, ExecutionException {

		CollectionReference userProfiles = userProfileService.getCollectionReference();
		if(params.getString("phoneNumber").startsWith("+")){
			params.put("phone",params.getString("phoneNumber").substring(1));
		}
		Query query = userProfiles.whereEqualTo("phone", params.getString("phoneNumber"));

		ApiFuture<QuerySnapshot> querySnapshot = query.get();
		UserProfile user = null;
		for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
			user = document.toObject(UserProfile.class);
			user.setProfileImage(params.getString("profileImage"));
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
	@PostMapping("sessionAttended")
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
	@PostMapping("referralsList")
	public JSONObject referralsList(@RequestBody JSONObject params) throws ExecutionException, InterruptedException {
		CollectionReference referrals = referralService.getCollectionReference();
		Query query = referrals.whereEqualTo("from", params.getString("from"));
//		Query query = referrals.whereEqualTo("from", "919427876625");
		List<Referral> referralsList = new ArrayList<>();
		ApiFuture<QuerySnapshot> querySnapshot = query.get();
		List<String> userIds = new ArrayList<>();
		List<QueryDocumentSnapshot> referralsSnapshot = querySnapshot.get().getDocuments();
		if(referralsSnapshot.size()!=0){
			for (DocumentSnapshot document : referralsSnapshot) {
				Referral obj = document.toObject(Referral.class);
				userIds.add(obj.getTo());
				referralsList.add(obj);
			}
		}

		CollectionReference userProfiles = userProfileService.getCollectionReference();
		Query userQuery = userProfiles.whereIn("phone",userIds);
		ApiFuture<QuerySnapshot> userQuerySnapshot = userQuery.get();
		List<QueryDocumentSnapshot> usersSnapshot =userQuerySnapshot.get().getDocuments();
		for (Referral ref:referralsList) {
			try {
				for(DocumentSnapshot userDocument : usersSnapshot) {
					UserProfile user = userDocument.toObject(UserProfile.class);
					if(StringUtils.equals(ref.getTo(),user.getPhone())==true) {
						ref.setToName(user.getName());
						ref.setToProfileImage(user.getProfileImage());
						break;
					}
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}

		referralsList.sort(new Comparator<Referral>() {
			@Override
			public int compare(Referral m1,Referral m2) {
				if(m1.getTime() == m2.getTime()){
					return 0;
				}
				return Long.parseLong(m1.getTime())> Long.parseLong(m2.getTime()) ? -1 : 1;
			}
		});

		JSONObject output = new JSONObject();
		output.put("referrals",referralsList);
		return output;
	}

	@PostMapping("topReferrals")
	public JSONObject topReferrals(@RequestBody JSONObject params) throws ExecutionException, InterruptedException {
		CollectionReference referrals = referralService.getCollectionReference();
		Integer top = params.getInteger("top");
		Query query = referrals.whereGreaterThanOrEqualTo("time", params.getString("time"));
		if(!StringUtils.isEmpty(params.getString("hasAttendedSession"))) {
			query = query.whereEqualTo("hasAttendedSession", params.getBoolean("hasAttendedSession"));
		}
		HashMap<String,Integer> ranking = new HashMap<>();
		List<Referral> referralsList = new ArrayList<>();
		ApiFuture<QuerySnapshot> querySnapshot = query.get();
		if(querySnapshot.get().getDocuments().size()!=0){
			for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
				Referral referral = document.toObject(Referral.class);
				if(ranking.containsKey(referral.getFrom()))
					ranking.put(referral.getFrom(),ranking.get(referral.getFrom())+1);
				else
					ranking.put(referral.getFrom(),1);
				referralsList.add(document.toObject(Referral.class));
			}
		}

		List<Pair<String,Integer>> sorted = new ArrayList<>();
		for(String key:ranking.keySet()){
			Pair<String,Integer> pair = new MutablePair<>(key,ranking.get(key));
			sorted.add(pair);
		}
		sorted.sort(new Comparator<Pair<String,Integer>>() {
			@Override
			public int compare(Pair<String,Integer> m1, Pair<String,Integer> m2) {
				if(m1.getRight() == m2.getRight()){
					return 0;
				}
				return m1.getRight()>m2.getRight() ? -1 : 1;
			}
		});
		sorted = sorted.subList(0,top>=sorted.size()?sorted.size():top);

		JSONObject output = new JSONObject();
		output.put("referrals",sorted);
		return output;
	}
}
