package com.startup.goHappy.entities.repository;

import com.google.cloud.firestore.Firestore;
import com.startup.goHappy.entities.model.UserMemberships;
import com.startup.goHappy.entities.service.AbstractFirestoreRepository;
import org.springframework.stereotype.Repository;

@Repository
public class UserMembershipsRepository extends AbstractFirestoreRepository<UserMemberships> {
    protected UserMembershipsRepository(Firestore firestore) {
        super(firestore, "UserMemberships");
    }
}
