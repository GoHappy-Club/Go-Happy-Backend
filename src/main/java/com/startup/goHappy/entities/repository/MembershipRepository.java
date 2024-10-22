package com.startup.goHappy.entities.repository;

import com.google.cloud.firestore.Firestore;
import com.startup.goHappy.entities.model.Membership;
import com.startup.goHappy.entities.service.AbstractFirestoreRepository;
import org.springframework.stereotype.Repository;

@Repository
public class MembershipRepository extends AbstractFirestoreRepository<Membership> {
    protected MembershipRepository(Firestore firestore) {
        super(firestore, "Membership");
    }
}
