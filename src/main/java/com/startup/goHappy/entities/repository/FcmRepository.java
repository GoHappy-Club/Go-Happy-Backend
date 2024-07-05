package com.startup.goHappy.entities.repository;

import com.google.cloud.firestore.Firestore;
import com.startup.goHappy.entities.model.Fcm;
import com.startup.goHappy.entities.service.AbstractFirestoreRepository;
import org.springframework.stereotype.Repository;


@Repository
public class FcmRepository extends AbstractFirestoreRepository<Fcm> {
    protected FcmRepository(Firestore firestore) {
        super(firestore, "Fcm");
    }
}