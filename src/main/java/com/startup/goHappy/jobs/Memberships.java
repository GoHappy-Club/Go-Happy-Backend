package com.startup.goHappy.jobs;


import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.startup.goHappy.entities.model.Membership;
import com.startup.goHappy.entities.model.UserMemberships;
import com.startup.goHappy.entities.repository.MembershipRepository;
import com.startup.goHappy.entities.repository.UserMembershipsRepository;
import com.startup.goHappy.enums.MembershipEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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

    @Scheduled(cron = "0 0 */6 * * *")
    public void giveMonthlyCoins() throws ExecutionException, InterruptedException {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        long thirtyDaysAgoInMillis = calendar.getTimeInMillis();

        CollectionReference userMembershipRef = userMembershipsService.getCollectionReference();
        CollectionReference membershipRef = membershipRepository.getCollectionReference();

        Query userQuery = userMembershipRef.whereLessThanOrEqualTo("lastCoinsCreditedDate", thirtyDaysAgoInMillis);
        ApiFuture<QuerySnapshot> querySnapshot = userQuery.get();

        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            UserMemberships userMembership = document.toObject(UserMemberships.class);
            MembershipEnum membershipType = userMembership.getMembershipType();
            Query query = membershipRef.whereEqualTo("membershipType", membershipType);
            ApiFuture<QuerySnapshot> querySnapshot1 = query.get();
            for (DocumentSnapshot document1 : querySnapshot1.get().getDocuments()) {
                Membership membership = document1.toObject(Membership.class);
                userMembership.setCoins(userMembership.getCoins() + membership.getCoinsPerMonth());
                break;
            }
        }
    }


}
