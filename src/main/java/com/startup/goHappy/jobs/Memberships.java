package com.startup.goHappy.jobs;


import com.alibaba.fastjson.JSONObject;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.gson.JsonObject;
import com.startup.goHappy.controllers.UserProfileController;
import com.startup.goHappy.entities.model.Membership;
import com.startup.goHappy.entities.model.UserMemberships;
import com.startup.goHappy.entities.model.UserProfile;
import com.startup.goHappy.entities.repository.MembershipRepository;
import com.startup.goHappy.entities.repository.UserMembershipsRepository;
import com.startup.goHappy.enums.MembershipEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;

@Service
public class Memberships {

    String EXPIRING_MEMBERSHIPS = "expiring_memberships";

    @Autowired
    UserMembershipsRepository userMembershipsService;

    @Autowired
    MembershipRepository membershipRepository;
    @Autowired
    private UserProfileController userProfileController;

    //    @Scheduled(cron = "0 0 */6 * * *")
    public void giveMonthlyCoins() throws ExecutionException, InterruptedException {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long thirtyDaysAgoInMillis = calendar.getTimeInMillis();

        CollectionReference userMembershipRef = userMembershipsService.getCollectionReference();
        CollectionReference membershipRef = membershipRepository.getCollectionReference();

        Query userQuery = userMembershipRef.whereEqualTo("lastCoinsCreditedDate", thirtyDaysAgoInMillis);
        ApiFuture<QuerySnapshot> querySnapshot = userQuery.get();
        calendar.set(Calendar.MONTH, 1);

        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            UserMemberships userMembership = document.toObject(UserMemberships.class);
            MembershipEnum membershipType = userMembership.getMembershipType();
            Query query = membershipRef.whereEqualTo("membershipType", membershipType);
            ApiFuture<QuerySnapshot> querySnapshot1 = query.get();
            for (DocumentSnapshot document1 : querySnapshot1.get().getDocuments()) {
                Membership membership = document1.toObject(Membership.class);
                userMembership.setCoins(userMembership.getCoins() + membership.getCoinsPerMonth());
                userMembership.setLastCoinsCreditedDate("" + calendar.getTimeInMillis());
                break;
            }
        }
    }

    //    @Scheduled(cron = "0 0 */6 * * *")
    public void notifyExpiringMembershipUsers() throws FirebaseMessagingException, ExecutionException, InterruptedException, IOException {
        CollectionReference userMembershipRef = userMembershipsService.getCollectionReference();

        long currTime = Calendar.getInstance().getTimeInMillis();
        currTime = currTime + 50L * 24 * 60 * 60 * 1000;

        Query userQuery = userMembershipRef.whereLessThanOrEqualTo("membershipEndDate", currTime);
        ApiFuture<QuerySnapshot> querySnapshot = userQuery.get();
        JSONObject getUserByPhoneParams = new JSONObject();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            UserMemberships userMembership = document.toObject(UserMemberships.class);
            getUserByPhoneParams.put("phoneNumber",userMembership.getPhone());
            JSONObject result = userProfileController.getUserByPhone(getUserByPhoneParams);
            UserProfile user = result.getObject("user", UserProfile.class);
        }
    }
}
