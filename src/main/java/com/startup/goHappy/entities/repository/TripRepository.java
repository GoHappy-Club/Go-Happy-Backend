package com.startup.goHappy.entities.repository;

import com.google.cloud.firestore.Firestore;
import com.startup.goHappy.entities.model.Trip;
import com.startup.goHappy.entities.service.AbstractFirestoreRepository;
import org.springframework.stereotype.Repository;


@Repository
public class TripRepository extends AbstractFirestoreRepository<Trip> {
    protected TripRepository(Firestore firestore) {
        super(firestore, "Trips");
    }
}