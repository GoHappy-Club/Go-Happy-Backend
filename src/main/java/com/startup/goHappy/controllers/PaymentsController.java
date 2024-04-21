package com.startup.goHappy.controllers;

import com.alibaba.fastjson.JSONObject;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.opencsv.CSVWriter;
import com.startup.goHappy.config.Firestore.FirestoreConfig;
import com.startup.goHappy.entities.model.PaymentLog;
import com.startup.goHappy.entities.model.Referral;
import com.startup.goHappy.entities.model.UserProfile;
import com.startup.goHappy.entities.repository.PaymentLogRepository;
import com.startup.goHappy.entities.repository.ReferralRepository;
import com.startup.goHappy.entities.repository.UserProfileRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.TestOnly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

@RestController
@RequestMapping("payments")
public class PaymentsController {


	@Autowired
	PaymentLogRepository paymentLogService;

	@Autowired
	FirestoreConfig firestoreConfig;


	public PaymentsController() {

	}

	@TestOnly
	public PaymentsController(PaymentLogRepository paymentLogService) {
		this.paymentLogService = paymentLogService;
	}


	@GetMapping("/download")
	public StreamingResponseBody downloadCsv(@RequestParam(required = false) String phone,
											 @RequestParam(required = false) String type,
											 @RequestParam(required = false) String paymentDate,
											 @RequestParam(required = false)int amount,
											 HttpServletResponse response)
			throws ExecutionException, InterruptedException, IOException {
		response.setContentType("text/csv");
		response.setHeader("Content-Disposition", "attachment; filename=\"payments.csv\"");

		CollectionReference paymentLogs = paymentLogService.getCollectionReference();
		Query query = null;
		if(!StringUtils.isEmpty(phone)) {
			query = paymentLogs.whereEqualTo("phone", phone);
		}
		if(!StringUtils.isEmpty(type)) {
			query = paymentLogs.whereEqualTo("type", type);
		}
		if(!StringUtils.isEmpty(paymentDate)) {
			query = paymentLogs.whereGreaterThanOrEqualTo("paymentDate", paymentDate);
		}
		if(Integer.valueOf(amount)!=null) {
			query = paymentLogs.whereGreaterThanOrEqualTo("amount", amount);
		}
		ApiFuture<QuerySnapshot> querySnapshot = query.get();
		List<String[]> payments = new ArrayList<>();
		String[] columnNames = {"ID", "Phone","Amount","Payment Date","Type"};
		payments.add(columnNames);
		for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
			PaymentLog pay = document.toObject(PaymentLog.class);
			if(pay==null){
				continue;
			}
			String[] userData = {
					pay.getId(),
					pay.getPhone(),
					pay.getAmount().toString(),
					pay.getPaymentDate(),
					pay.getType()
			};
			payments.add(userData);
		}
		CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(response.getOutputStream()));
		csvWriter.writeAll(payments);
		csvWriter.close();

		return OutputStream::flush;
	}

}
