package com.startup.goHappy.entities.repository;

import com.google.cloud.firestore.Firestore;
import com.startup.goHappy.entities.model.Ratings;
import com.startup.goHappy.entities.service.AbstractFirestoreRepository;
import org.springframework.stereotype.Repository;

@Repository
public class RatingsRepository extends AbstractFirestoreRepository<Ratings> {
    protected RatingsRepository(Firestore firestore) {
        super(firestore,"Ratings");
    }
}
