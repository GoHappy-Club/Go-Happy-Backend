package com.startup.goHappy.jobs;


import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.startup.goHappy.entities.repository.UserMembershipsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.ExecutionException;

@Service
public class Memberships {

    String EXPIRING_MEMBERSHIPS = "expiring_memberships";

    @Autowired
    UserMembershipsRepository userMembershipsService;

//    @Scheduled(cron = "0 0 */6 * * *")
    public void expiringMemberships() throws ExecutionException, InterruptedException {
        CollectionReference userMemberships = userMembershipsService.getCollectionReference();
        long currentTime = new Date().getTime();
        Query query = userMemberships.whereLessThanOrEqualTo("membershipEndDate", currentTime + 2L * 30 * 24 * 60 * 60 * 1000);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {

        }
    }


}
