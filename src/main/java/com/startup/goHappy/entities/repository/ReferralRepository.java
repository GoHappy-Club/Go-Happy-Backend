package com.startup.goHappy.entities.repository;

import com.google.cloud.firestore.Firestore;
import com.startup.goHappy.entities.model.Event;
import com.startup.goHappy.entities.model.Referral;
import com.startup.goHappy.entities.service.AbstractFirestoreRepository;
import org.springframework.stereotype.Repository;


@Repository
public class ReferralRepository extends AbstractFirestoreRepository<Referral> {
    protected ReferralRepository(Firestore firestore) {
        super(firestore, "Referrals");
    }
}