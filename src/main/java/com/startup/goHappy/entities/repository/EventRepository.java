package com.startup.goHappy.entities.repository;

import org.springframework.stereotype.Repository;

import com.google.cloud.firestore.Firestore;
import com.startup.goHappy.entities.model.Event;
import com.startup.goHappy.entities.model.UserProfile;
import com.startup.goHappy.entities.service.AbstractFirestoreRepository;

import java.util.List;


@Repository
public class EventRepository extends AbstractFirestoreRepository<Event> {
    protected EventRepository(Firestore firestore) {
        super(firestore, "Events");
    }
}