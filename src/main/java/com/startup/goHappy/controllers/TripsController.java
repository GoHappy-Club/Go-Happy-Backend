package com.startup.goHappy.controllers;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.startup.goHappy.entities.model.*;
import com.startup.goHappy.entities.model.Properties;
import com.startup.goHappy.entities.repository.PropertiesRepository;
import com.startup.goHappy.entities.repository.TripRepository;
import com.startup.goHappy.entities.repository.UserVouchersRepository;
import com.startup.goHappy.entities.repository.VouchersRepository;
import com.startup.goHappy.enums.VoucherStatusEnum;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("trips")
public class TripsController {

	@Autowired
	TripRepository tripRepository;
    @Autowired
    UserVouchersRepository userVouchersService;
    @Autowired
    MembershipController membershipController;
    @Autowired
    VouchersRepository vouchersService;

	@GetMapping("list")
	public JSONObject list() throws Exception {
		Iterable<Trip> trips = tripRepository.retrieveAll();
		List<Trip> result = IterableUtils.toList(trips);
		JSONObject output = new JSONObject();
		output.put("trips", result);
		return output;
	}

	@GetMapping("upcoming")
	public JSONObject upcoming() throws Exception {
		CollectionReference tripsRef = tripRepository.getCollectionReference();
		Instant instance2 = java.time.Instant.now();
		ZonedDateTime zonedDateTime = java.time.ZonedDateTime
				.ofInstant(instance2,java.time.ZoneId.of("Asia/Kolkata"));
		Query queryNew = tripsRef.whereGreaterThanOrEqualTo("startTime", ""+zonedDateTime.toInstant().toEpochMilli());

		ApiFuture<QuerySnapshot> querySnapshotNew = queryNew.get();

		Set<Trip> trips = new HashSet<>();
		try {
			for (DocumentSnapshot document : querySnapshotNew.get().getDocuments()) {
				trips.add(document.toObject(Trip.class));
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		List<Trip> result = IterableUtils.toList(trips);
		result.sort(Comparator.comparing(Trip::getStartTime));
		JSONObject output = new JSONObject();
		output.put("trips", result);
		return output;
	}

	@GetMapping("past")
	public JSONObject past() throws Exception {
		CollectionReference tripsRef = tripRepository.getCollectionReference();
		Instant instance2 = java.time.Instant.now();
		ZonedDateTime zonedDateTime = java.time.ZonedDateTime
				.ofInstant(instance2, java.time.ZoneId.of("Asia/Kolkata"));
		Query queryNew = tripsRef.whereLessThan("startTime", "" + zonedDateTime.toInstant().toEpochMilli());

		ApiFuture<QuerySnapshot> querySnapshotNew = queryNew.get();

		Set<Trip> trips = new HashSet<>();
		try {
			for (DocumentSnapshot document : querySnapshotNew.get().getDocuments()) {
				trips.add(document.toObject(Trip.class));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<Trip> result = IterableUtils.toList(trips);
		result.sort(Comparator.comparing(Trip::getStartTime));
		Collections.reverse(result);
		JSONObject output = new JSONObject();
		output.put("trips", result);
		return output;
	}

	@PostMapping("add")
	public void add(@RequestBody JSONObject tripObject) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		Trip trip = objectMapper.readValue(tripObject.toJSONString(), Trip.class);
		trip.setId(UUID.randomUUID().toString());
		tripRepository.save(trip);

		return ;
	}

	@GetMapping("getDetails/{id}")
	public JSONObject details(@PathVariable String id, @RequestParam(required = false) String phone) throws Exception {
		CollectionReference tripsRef = tripRepository.getCollectionReference();
		Instant instance2 = java.time.Instant.now();
		Query queryNew = tripsRef.whereEqualTo("id", id);

		ApiFuture<QuerySnapshot> querySnapshotNew = queryNew.get();

		Trip trip = null;
		try {
			for (DocumentSnapshot document : querySnapshotNew.get().getDocuments()) {
				trip = document.toObject(Trip.class);
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		List<Map<String,Object>> resultantUserVouchers = new ArrayList<>();
		if(phone != null) {
			CollectionReference voucherRef = vouchersService.getCollectionReference();
            ApiFuture<QuerySnapshot> snapshot = voucherRef.whereEqualTo("category", "trips").get();
            Vouchers voucherTrips = null;
            for(DocumentSnapshot document : snapshot.get().getDocuments()) {
                voucherTrips = document.toObject(Vouchers.class);
            }
            CollectionReference userVouchersRef = userVouchersService.getCollectionReference();
			if(voucherTrips != null) {
                Query query = userVouchersRef.whereEqualTo("phone", phone).whereEqualTo("voucherId", voucherTrips.getId()).whereEqualTo("status", VoucherStatusEnum.ACTIVE);
                ApiFuture<QuerySnapshot> snapshotApiFuture = query.get();
                List<UserVouchers> usersVouchers = new ArrayList<>();
                for (DocumentSnapshot document : snapshotApiFuture.get().getDocuments()) {
                    UserVouchers usersVoucher = document.toObject(UserVouchers.class);
                    usersVouchers.add(usersVoucher);
                }
                Set<String> voucherIds = usersVouchers.stream().map(UserVouchers::getVoucherId).collect(Collectors.toSet());
                Map<String, Vouchers> categoryToVoucherMap = voucherIds.stream()
                        .map(voucherId -> {
                            try {
                                return membershipController.getVoucherDetailFromVoucherId(voucherId);
                            } catch (ExecutionException | InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .collect(Collectors.toMap(Vouchers::getId, voucher -> voucher));
                System.out.println(categoryToVoucherMap);
                ObjectMapper objectMapper = new ObjectMapper();
                for (UserVouchers userVoucher : usersVouchers) {
                    Map<String, Object> userVoucherMap = objectMapper.convertValue(userVoucher, Map.class);
                    userVoucherMap.put("title", categoryToVoucherMap.get(userVoucher.getVoucherId()).getTitle());
                    userVoucherMap.put("image", categoryToVoucherMap.get(userVoucher.getVoucherId()).getImage());
                    userVoucherMap.put("description", categoryToVoucherMap.get(userVoucher.getVoucherId()).getDescription());
                    userVoucherMap.put("percent", categoryToVoucherMap.get(userVoucher.getVoucherId()).getPercent());
                    userVoucherMap.put("limit", categoryToVoucherMap.get(userVoucher.getVoucherId()).getLimit());
                    userVoucherMap.put("value", categoryToVoucherMap.get(userVoucher.getVoucherId()).getValue());
                    resultantUserVouchers.add(userVoucherMap);
                }
            }
        }
		JSONObject output = new JSONObject();
		output.put("details", trip);
		output.put("vouchers",resultantUserVouchers);
		return output;
	}
}
